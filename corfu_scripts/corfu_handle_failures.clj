; Query endpoint status given the endpoint as the first arg
(in-ns 'org.corfudb.shell) ; so our IDE knows what NS we are using

(import org.docopt.Docopt) ; parse some cmdline opts
(require 'clojure.pprint)
(require 'clojure.java.shell)
(import org.corfudb.runtime.view.Layout)
(def usage "corfu_handle_failures, initiates failure handler on all Management Servers.
Usage:
  corfu_handle_failures -c <config> [-e [-u <keystore> -f <keystore_password_file>] [-r <truststore> -w <truststore_password_file>] [-g -o <username_file> -j <password_file>]]
Options:
  -c <config>, --config <config>              Configuration string to use.
  -e, --enable-tls                                                                       Enable TLS.
  -u <keystore>, --keystore=<keystore>                                                   Path to the key store.
  -f <keystore_password_file>, --keystore-password-file=<keystore_password_file>         Path to the file containing the key store password.
  -r <truststore>, --truststore=<truststore>                                             Path to the trust store.
  -w <truststore_password_file>, --truststore-password-file=<truststore_password_file>   Path to the file containing the trust store password.
  -g, --enable-sasl-plain-text-auth                                                      Enable SASL Plain Text Authentication.
  -o <username_file>, --sasl-plain-text-username-file=<username_file>                    Path to the file containing the username for SASL Plain Text Authentication.
  -j <password_file>, --sasl-plain-text-password-file=<password_file>                    Path to the file containing the password for SASL Plain Text Authentication.
  -h, --help     Show this screen.
")

; Parse the incoming docopt options.
(def localcmd (.. (new Docopt usage) (parse *args)))

(defn send-trigger [server]
       (try
         (do (get-router server localcmd)
             (.get (.initiateFailureHandler (get-management-client)))
             (println "Failure handler on" server "started."))
         (catch Exception e
           (println "Exception :" server ":" (.getMessage e))))
      )

(defn start-fh-runtime []
      ; Get the runtime.
      (get-runtime (.. localcmd (get "--config")) localcmd)
      (connect-runtime)
      (def layout-view (get-layout-view))

      ; Send trigger to everyone in the layout
      (let [layout (.. (get-layout-view) (getLayout))]
           (doseq [server (.getAllServers layout)]
                (send-trigger layout))

           (println "Initiation completed !")
       ))

(defn start-fh [server]
      ; Send trigger to one of the nodes and let it handle the failures and update the layout.
      (send-trigger server)
      ; Once the layout is updated, we connect to the runtime and send the trigger to all
      ; nodes in the layout.
      (start-fh-runtime)
      )


; determine whether options passed correctly
(cond (.. localcmd (get "--config")) (start-fh (.. localcmd (get "--config")))
      :else (println "Unknown arguments.")
      )
