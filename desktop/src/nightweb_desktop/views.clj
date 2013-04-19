(ns nightweb-desktop.views
  (:use [clojure.java.io :only [resource]]
        [clojure.xml :only [parse]]
        [nightweb.formats :only [url-encode]]
        [nightweb.db_tiles :only [get-user-tiles
                                  get-category-tiles]]))

(def strings (-> (resource "strings.xml")
                 (.getFile)
                 (parse)
                 (get :content)))

(defn get-string
  [res-name]
  (if (keyword? res-name)
    (-> (filter #(= (get-in % [:attrs :name]) (name res-name))
                strings)
        (first)
        (get :content)
        (first))
    res-name))

(defn get-tab-view
  [params show-me-tab?]
  (for [button (concat [(when show-me-tab?
                          {:type nil :title "Me"})]
                       [{:type :user :title "Users"}
                        {:type :post :title "Posts"}])]
    [:li {:class (when (= (get params :type) (get button :type)) "active")}
     [:a {:href (url-encode button (if show-me-tab? "/?" "/c?"))}
      (get button :title)]]))

(defn get-menu-view
  []
  (for [button [{:class "foundicon-search"
                 :title "Search"}
                {:class "foundicon-plus"
                 :title "New Post"}
                {:class "foundicon-page"
                 :title "Link"}
                {:class "foundicon-settings"
                 :title "Settings"}]]
    [:li [:a {:href "#"}
          [:i {:class (get button :class)}]
          (str "&nbsp;" (get button :title))]]))

(defn get-action-bar-view
  [tab-view]
  [:div {:class "sticky"}
   [:nav {:class "top-bar"}
    [:section {:class "top-bar-section"}
     (when tab-view
       [:ul {:class "left"} tab-view])
     [:ul {:class "right"} (get-menu-view)]]]
   [:div {:class "clear"}]])

(defn get-grid-view
  [content]
  [:ul {:class "grid-view"}
   (for [item content]
     (let [bg (get item :background)
           title (get-string (or (get item :title)
                                 (get item :body)
                                 (get item :tag)))
           add-emphasis? (get item :add-emphasis?)]
       [:li {:style (format "background: url('img/%s.png') no-repeat;
                             background-size: 100%%;"
                            (if bg (name bg)))}
        (if add-emphasis? [:strong title] [:div title])]))])

(defn get-post-view
  [params]
  (get-grid-view []))

(defn get-user-view
  [params]
  (let [tiles (get-user-tiles params (fn [_ _]) (fn [_ _]) (fn [_ _]))]
    (get-grid-view tiles)))

(defn get-category-view
  [params]
  (let [tiles (get-category-tiles params)]
    (get-grid-view tiles)))
