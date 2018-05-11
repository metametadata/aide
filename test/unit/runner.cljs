(ns unit.runner
  (:require [cljs.test]
            [doo.runner :refer-macros [doo-all-tests]]

            [unit.core]
            [unit.var-event]))

(doo-all-tests #"unit\..*")