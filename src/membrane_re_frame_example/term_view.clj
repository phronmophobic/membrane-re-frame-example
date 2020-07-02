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


(def text-boxes (atom {}))
(def text-box-dispatch (component/default-handler text-boxes))
(defn get-text-box [tid text]
  (let [all-data @text-boxes
        data (get all-data tid)
        args (apply concat
                    [:text text]
                    [:focus (get all-data ::focus)]
                    (for [[k v] data
                          :when (not= k :text)]
                      [k v]))
        $args [:$text [(list 'get tid) :text]
               :$font [(list 'get tid) :font]
               :$textarea-state [(list 'get tid) :textarea-state]
               :$extra [(list 'get tid) :border?]
               :$focus [::focus]]]
    (ui/on-bubble
     (fn [effects]
       (text-box-dispatch :set [(list 'get tid) :text] text)
       (run! #(apply text-box-dispatch %) effects)
       (let [new-data (get @text-boxes tid)
             new-text (:text new-data)]
         (when (not= new-text text)
           [[:change new-text]])))
     (apply lanterna/textarea (concat args $args)))))



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
       (fn [s]
         [[:set-input-text input-id s]])
       (get-text-box input-id text))))))



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
      (fn [s]
        [[:save id s]])
      (get-text-box input-id title)))))


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
