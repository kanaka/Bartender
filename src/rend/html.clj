(ns rend.html
  (:require [hiccup.core :as hiccup]
            [clojure.math.combinatorics :refer [combinations]]))

(def RED "#ff8080")
(def GREEN "#80ff80")


(defn render-report-row [browsers logs sth]
  (let [l (nth logs sth)
        idx (+ 1 sth)
        pass (:result l)
        diffs (:diffs l)
        violations (:violations l)
        html (:html l)]
    (vec
      (concat
        [:tr
         [:td idx]
         [:td
          {:style (if pass
                    (str "background-color: " GREEN)
                    (str "background-color: " RED))}
          (if pass "PASS" "FAIL")]
         [:td
          [:a {:href (str idx ".html")
               :title (str (hiccup/html html))}
           "html"]
          " / "
          [:a {:href (str idx ".html.txt")}
           "txt"]]
         [:td "&nbsp;"]]
        (for [browser browsers]
          [:td
           (if (= (disj (set browsers) browser)
                  (set (keys (get violations browser))))
             {:style (str "vertical-align: top; text-align: center; "
                          "background-color: " RED)}
             {:style "vertical-align: top; text-align: center"})
           [:a {:style "padding-left: 2px; padding-right: 2px"
                :href (str idx "_" (:id browser) ".png")}
            [:span.tlink "png"]
            [:img.thumb {:style "display: none"
                         :src (str idx "_" (:id browser)
                                   "_thumb.png")}]]])
        [[:td "&nbsp;"]
         [:td {:style "vertical-align: top"}
          [:a {:href (str idx "_avg.png")}
           [:span.tlink "png"]
           [:img.thumb {:style "display: none"
                        :src (str idx "_avg_thumb.png")}]]]
         [:td "&nbsp;"]]
        (for [[ba bb] (combinations browsers 2)
              :let [odiff (get-in diffs [ba bb])]]
          [:td
           (if (or (get violations ba)
                   (get violations bb))
             {:style (str "vertical-align: top; text-align: center; background-color: " RED)}
             {:style (str "vertical-align: top; text-align: center")})
           [:a {:href (str idx "_diff_" (:id ba)
                           "_" (:id bb) ".png")}
            [:img.thumb {:style "display: none"
                         :src (str idx "_diff_" (:id ba)
                                   "_" (:id bb) "_thumb.png")}]
            [:br.thumb {:style "display: none"}]
            (format "%.6f" odiff)]])))))

(def toggle-thumbs-js "
  function toggle_thumbs() {
    var toggleb = document.getElementById('toggle');
    var thumb_display = 'none',
        tlink_display = 'none';
    if (toggleb.value === 'Show Thumbnails') {
      history.pushState(null, null, '#thumbs')
      toggleb.value = 'Hide Thumbnails'
      thumb_display = 'inline';
    } else {
      history.pushState(null, null, '#')
      toggleb.value = 'Show Thumbnails'
      tlink_display = 'inline';
    }
    for (var x of document.getElementsByClassName('thumb')) {
      x.style.display = thumb_display;
    }
    for (var x of document.getElementsByClassName('tlink')) {
      x.style.display = tlink_display;
    }
  }
  if (location.hash.indexOf('thumbs') > -1) {
    toggle_thumbs()
  }
")

;; Generate an HTML index page for the current test results
(defn render-report [cfg state]
  (let [logs (:log state)
        threshold (-> cfg :compare :threshold)
        browsers (:browsers cfg)]
    (hiccup/html
      [:html
       [:style "a {text-decoration: none}"]
       [:body
        [:div "Threshold value: " (format "%.6f" threshold)]
        [:br]
        [:input#toggle {:type "button"
                        :value "Show Thumbnails"
                        :onclick "toggle_thumbs()"}]
        [:br][:br]
        (vec
          (concat
            [:table {:style "border-spacing: 4px 0px"}
             (vec
               (concat
                 [:tr [:th "Test"] [:th "Result"] [:th "Html"]
                  [:th "&nbsp;"]]
                 (for [browser browsers]
                   [:th (str (:id browser))])
                 [[:th "&nbsp;"] [:th "Average"] [:th "&nbsp;"]]
                 (for [[ba bb] (combinations browsers 2)]
                   [:th (str (:id ba) "&Delta;" (:id bb))])))]
            (for [i (range (count logs))]
              (render-report-row browsers logs i))))
        [:script toggle-thumbs-js]]])))
