# slash

A small Clojure library designed to handle and route Discord interactions, both for gateway events and incoming webhooks. 

slash is environment-agnostic, extensible through middleware and works directly with Clojure data (no JSON parsing/printing included).

[![Clojars Project](https://img.shields.io/clojars/v/com.github.johnnyjayjay/slash.svg)](https://clojars.org/com.github.johnnyjayjay/slash)

**slash is currently in a Proof-of-Concept-phase and more features are to be added.**\
Such features include:
 - Add more middleware: argument validation, permission checks, ...

## Command Structure Definition

slash provides utilities to define slash commands in `slash.command.structure`.

Once you are familiar with [how slash commands are structured](https://discord.com/developers/docs/interactions/application-commands), the functions should be self-explanatory.

Examples:

``` clojure
(require '[slash.command.structure :refer :all])

(def input-option (option "input" "Your input" :string :required true))

(def echo-command
  (command
   "echo"
   "Echoes your input"
   :options
   [input-option]))

(def fun-commands
  (command
   "fun"
   "Fun commands"
   :options
   [(sub-command
     "reverse"
     "Reverse the input"
     :options
     [input-option
      (option "words" "Reverse words instead of characters?" :boolean)])
    (sub-command
     "mock"
     "Spongebob-mock the input"
     :options
     [input-option])]))
```

## Component Structure Definition

slash also provides similar utilities to create [message components](https://discord.com/developers/docs/interactions/message-components).

Examples:

``` clojure
(require '[slash.component.structure :refer :all])

(def my-components
  [(action-row
    (button :danger "unsubscribe" :label "Turn notifications off")
    (button :success "subcribe" :label "Turn notifications on"))
   (action-row
    (select-menu
     "language"
     [(select-option "English" "EN" :emoji {:name "🇬🇧"})
      (select-option "French" "FR" :emoji {:name "🇫🇷"})
      (select-option "Spanish" "ES" :emoji {:name "🇪🇸"})]
     :placeholder "Language"))])
```

## Routing 

You can use slash to handle interaction events based on their type.

``` clojure
(slash.core/route-interaction handler-map interaction-event)
```

`handler-map` is a map containing handlers for the different types of interactions that may occur. E.g. 

``` clojure
{:ping ping-handler
 :application-command command-handler
 :message-component component-handler}
```

You can find default handler maps for both gateway and webhook environments in `slash.gateway`/`slash.webhook` respectively.

### Commands

slash offers further routing middleware and utilities specifically for slash commands. The API is heavily inspired by [compojure](https://github.com/weavejester/compojure). 

Simple, single-command example:

``` clojure
(require '[slash.command :as cmd] 
         '[slash.response :as rsp :refer [channel-message ephemeral]]) ; The response namespace provides utility functions to create interaction responses

(cmd/defhandler echo-handler
  ["echo"] ; Command path
  _interaction ; Interaction binding - whatever you put here will be bound to the entire interaction
  [input] ; Command options - can be either a vector or a custom binding (symbol, map destructuring, ...)
  (channel-message {:content input}))
```

You can now use `echo-handler` as a command handler to call with a command interaction event and it will return the response if it is an `echo` command or `nil` if it's not.

An example with multiple (sub-)commands:

``` clojure
(require '[clojure.string :as str])

(cmd/defhandler reverse-handler
  ["reverse"]
  _
  [input words]
  (channel-message
   {:content (if words
               (->> #"\s+" (str/split input) reverse (str/join " "))
               (str/reverse input))}))

(cmd/defhandler mock-handler
  ["mock"]
  _
  [input]
  (channel-message
   {:content (->> input
                  (str/lower-case)
                  (map #(cond-> % (rand-nth [true false]) Character/toUpperCase))
                  str/join)}))
                  
(cmd/defhandler unknown-handler
  [unknown] ; Placeholders can be used in paths too
  {{{user-id :id} :user} :member} ; Using the interaction binding to get the user who ran the command
  _ ; no options
  (-> (channel-message {:content (str "I don't know the command `" unknown "`, <@" user-id ">.")})
      ephemeral))
      
(cmd/defpaths command-paths
  (cmd/group ["fun"] ; common prefix for all following commands
    reverse-handler 
    mock-hander
    unknown-handler))
```

Similar to the previous example, `command-paths` can now be used as a command handler. It will call each of its nested handlers with the interaction and stop once a handler is found that does not return `nil`.

### Autocomplete 

You can also use the command routing facilities to provide autocomplete for your commands.

``` clojure
;; Will produce autocompletion for command `/foo bar` on option `baz`, using the partial value of `baz` in the process
(cmd/defhandler foo-bar-autocompleter
  ["foo" "bar"]
  {{:keys [focused-option]} :data}
  [baz]
  (case focused-option 
    :baz (rsp/autocomplete-result (map (partial str baz) [1 2 3]))))
```

### Full Webhook Example 

For this example, I use the ring webserver specification.

Using [ring-json](https://github.com/ring-clojure/ring-json) and [ring-discord-auth](https://github.com/JohnnyJayJay/ring-discord-auth) we can create a ring handler for accepting outgoing webhooks.

``` clojure
(require '[slash.webhook :refer [webhook-defaults]]
         '[ring-discord-auth.ring :refer [wrap-authenticate]]
         '[ring.middleware.json :refer [wrap-json-body wrap-json-response]])

(def ring-handler
  (-> (partial slash.core/route-interaction
               (assoc webhook-defaults :application-command command-paths))
      wrap-json-response
      (wrap-json-body {:keyword? true})
      (wrap-authenticate "application public key")))
```

### Full Gateway Example

For this example, I use [discljord](https://github.com/IGJoshua/discljord).

You also see the use of the `wrap-response-return` middleware for the interaction handler, which allows you to simply return the interaction
responses from your handlers and let the middleware respond via REST. You only need to provide a callback that specifies how to respond to the interaction (as I'm using discljord here, I used its functions for this purpose).

``` clojure
(require '[discljord.messaging :as rest]
         '[discljord.connections :as gateway]
         '[discljord.events :as events]
         '[clojure.core.async :as a]
         '[slash.gateway :refer [gateway-defaults wrap-response-return]])

(let [rest-conn (rest/start-connection! "bot token")
      event-channel (a/chan 100)
      gateway-conn (gateway/connect-bot! "bot token" event-channel :intents #{})
      event-handler (-> slash.core/route-interaction
                        (partial (assoc gateway-defaults :application-command command-paths))
                        (wrap-response-return (fn [id token {:keys [type data]}]
                                                (rest/create-interaction-response! rest-conn id token type :data data))))]
  (events/message-pump! event-channel (partial events/dispatch-handlers {:interaction-create [#(event-handler %2)]})))
```
This is a very quick and dirty example. More in-depth documentation and tutorials will follow soon.

## clj-kondo support for macros

You can find a clj-kondo config that gets rid of "unresolved symbol" warnings in [.clj-kondo/](./.clj-kondo). Just copy [the hooks](./.clj-kondo/hooks) to your clj-kondo config folder (preserving the directory structure, of course!) and add this to your `config.edn`:

``` clojure
{:hooks {:analyze-call {slash.command/handler hooks.slash/handler
                        slash.command/defhandler hooks.slash/defhandler
                        slash.command/group hooks.slash/group
                        slash.command/defpaths hooks.slash/defpaths}}}
```

## License

Copyright © 2021-2023 JohnnyJayJay

Licensed under the MIT license.
