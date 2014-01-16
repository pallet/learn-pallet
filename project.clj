(defproject learn-pallet "1.0-SNAPSHOT"
  :description "Pallet exercises for the big and the small!"
  :url "http://palletops.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.logging "0.2.3"]
                 [ch.qos.logback/logback-classic "1.0.9"]
                 [alembic "0.1.3"]
                 [com.palletops/pallet "0.8.0-RC.6"]
                 [com.palletops/pallet-repl "0.8.0-beta.2"]]
  :repositories {"sonatype"
                 "http://oss.sonatype.org/content/repositories/releases"}
  :jvm-opts ["-Djava.awt.headless=true"])
