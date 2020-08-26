(ns membrane-re-frame-example.views
  (:require [membrane.skia :as skia]
            [membrane.basic-components :as basic]
            [membrane.re-frame :as memframe]
            [membrane.ui :as ui
             :refer
             [horizontal-layout
              vertical-layout
              on]]
            [re-frame.core :as rf :refer [reg-event-db reg-event-fx inject-cofx path after reg-sub subscribe dispatch]]

            membrane-re-frame-example.db
            membrane-re-frame-example.subs
            membrane-re-frame-example.events
            ))




(defn todo-input [{:keys [id title on-save on-stop]}]
  (let [input-id [:todo-input id]
        text @(rf/subscribe [:input-text input-id])]
    (horizontal-layout
     (ui/button "Add Todo"
                (fn []
                  [(on-save text)
                   [:set-input-text input-id ""]]))
     (ui/wrap-on
      :key-press
      (fn [handler s]
        (if (= s :enter)
          [(on-save text)
           [:set-input-text input-id ""]]
          (handler s)))
      (ui/translate
       10 5
       (on
        :change
        (fn [s]
          [[:set-input-text input-id s]])
        (memframe/get-text-box input-id text)))))))


(defn delete-X []
  (ui/with-style :membrane.ui/style-stroke
    (ui/with-color
      [1 0 0]
      (ui/with-stroke-width
        3
        [(ui/path [0 0]
                  [10 10])
         (ui/path [10 0]
                  [0 10])]))))

(defn todo-item
  [{:keys [id done title] :as todo}]
  (let [;;editing @(rf/subscribe [:extra [:editing (:id todo)]])
        input-id [:todo-input id]
        ]
    (horizontal-layout
     (ui/translate 0 5
      (on
       :mouse-down
       (fn [_]
         [[:delete-todo id]])
       (delete-X)))
     (ui/spacer 5 0)
     (ui/translate 0 4
                   (on
                    :mouse-down
                    (fn [_]
                      [[:toggle-done id]])
                    (ui/checkbox done)))
     (ui/spacer 5 0)
     
     (on
      :change
      (fn [s]
        [[:save id s]])
      (memframe/get-text-box input-id title)))))


(defn task-list
  []
  (let [visible-todos @(subscribe [:visible-todos])
        all-complete? @(subscribe [:all-complete?])]
    (apply
     vertical-layout
     (interpose
      (ui/spacer 0 10)
      (for [todo visible-todos]
        (todo-item todo))))))


(defn footer-controls
  []
  (let [[active done] @(subscribe [:footer-counts])
        showing       @(subscribe [:showing])
        a-fn          (fn [filter-kw txt]
                        (on
                         :mouse-down
                         (fn [_]
                           [[:set-showing filter-kw]])
                         (ui/with-color (if (= filter-kw showing)
                                          [0 0 0]
                                          [0.7 0.7 0.7])
                           (ui/label txt))))]
    (vertical-layout
     (ui/label (str active
                    " "
                    (if (= 1 active)
                      "item"
                      "items")
                    " left"))
     (ui/spacer 0 5)
     (apply
      horizontal-layout
      (interpose
       (ui/spacer 5 0)
       (for [[kw txt] [[:all "All"]
                       [:active "Active"]
                       [:done "Completed"]]]
         (a-fn kw txt))))
     (ui/spacer 0 5)
     (when (pos? done)
       (ui/button "Clear completed"
                  (fn []
                    [[:clear-completed]]))))))


(defn task-entry
  []
  (todo-input
   {:id "new-todo"
    :placeholder "What needs to be done?"
    :on-save #(when (seq %)
                [:add-todo %])}))


(defn todo-app
  []
  (ui/translate
   10 10
   (vertical-layout
    (task-entry)
    (ui/spacer 0 20)
    (when (seq @(subscribe [:todos]))
      (task-list))
    (ui/spacer 0 20)
    (footer-controls)
    )))


(defn -main [& args]
  (dispatch [:initialize-db])  
  (skia/run #(memframe/re-frame-app (todo-app))))


(defn test-scrollview [text]
    [(ui/translate 10 10
                   (memframe/get-scrollview :my-scrollview [300 300]
                                            (ui/label text)))])

(comment


  (def lorem-ipsum (clojure.string/join
                    "\n"
                    (repeatedly 800
                                (fn []
                                  (clojure.string/join
                                   (repeatedly (rand-int 50)
                                               #(rand-nth "abcdefghijklmnopqrstuvwxyz ")))))))

  (skia/run #(memframe/re-frame-app (test-scrollview lorem-ipsum))))
