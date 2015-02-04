(ns lunchlotto.common.forms
  (:require [hiccup.form :as hiccup]
            [ring.util.anti-forgery :as ring]))

(defn make-field [func field {:keys [:label :value :error :help-text :opts]}]
  [:div.form-group
   {:class (when error "has-error")}
   (hiccup/label {:class "control-label"} field label)
   (func (merge {:id field :class "form-control" :value value} opts) field)
   (cond
     error [:span.help-block error]
     help-text [:span.help-block help-text])])

(defmulti field first)

(defmethod field :text-field
  [[_ name opts]]
  (make-field hiccup/text-field name opts))

(defmethod field :password-field
  [[_ name opts]]
  (make-field hiccup/password-field name opts))

(defmethod field :hidden-field
  [[_ name opts]]
  (hiccup/hidden-field {:id name :value (:value opts)} name))

(defn build-form
  [method url button-text & body]
  (hiccup/form-to
    [method url]
    (ring/anti-forgery-field)
    (map field body)
    [:div.form-group
     (hiccup/submit-button {:class "btn btn-primary"} button-text)]))
