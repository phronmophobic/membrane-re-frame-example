(ns membrane-re-frame-example.term-view
  (:require [membrane.lanterna :as lanterna]
            [membrane.component :as component]
            [membrane.re-frame :as memframe]
            [membrane.ui :as ui
             :refer
             [horizontal-layout
              vertical-layout
              on]]
            [re-frame.core :as rf :refer [reg-event-db reg-event-fx inject-cofx path after reg-sub subscribe dispatch]]

            membrane-re-frame-example.db
            membrane-re-frame-example.subs
            membrane-re-frame-example.events)
  (:gen-class))




(memframe/defrf textbox lanterna/textarea [tid {:keys [text]}])

(defn todo-input [{:keys [id title on-save on-stop]}]
  (let [input-id [:todo-input id]
        text @(rf/subscribe [:input-text input-id])]
    (horizontal-layout
     (lanterna/button "Add Todo"
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
      (on
       :change
       (fn [k v]
         (when (= k :text)
           [[:set-input-text input-id v]]))
       (textbox input-id {:text text}))))))



(defn todo-item
  [{:keys [id done title] :as todo}]
  (let [;;editing @(rf/subscribe [:extra [:editing (:id todo)]])
        input-id [:todo-input id]
        ]
    (horizontal-layout
     
     (ui/translate 0 1
                   (horizontal-layout
                    (on
                     :mouse-down
                     (fn [_]
                       [[:toggle-done id]])
                     (lanterna/checkbox-view done))
                    (on
                     :mouse-down
                     (fn [_]
                       [[:delete-todo id]])
                     (ui/with-color [1 0 0]
                       (lanterna/label "X")))))
     (on
      :change
      (fn [k v]
        (when (= k :text)
          [[:save id v]]))
      (textbox input-id {:text title})))))


(defn task-list
  []
  (let [visible-todos @(subscribe [:visible-todos])
        all-complete? @(subscribe [:all-complete?])]
    (apply
     vertical-layout
     (for [todo visible-todos]
       (todo-item todo)))))


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
                           (lanterna/label txt))))]
    (vertical-layout
     (lanterna/label (str active
                          " "
                          (if (= 1 active)
                            "item"
                            "items")
                          " left"))
     (apply
      horizontal-layout
      (for [[kw txt] [[:all "All"]
                      [:active "Active"]
                      [:done "Completed"]]]
        (a-fn kw txt)))
     (when (pos? done)
       (lanterna/button "Clear completed"
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
  (vertical-layout
   (task-entry)
   (when (seq @(subscribe [:todos]))
     (task-list))
   (footer-controls)
   ))


(defn -main [& args]
  (dispatch [:initialize-db])  
  (lanterna/run #(memframe/re-frame-app (todo-app))))
