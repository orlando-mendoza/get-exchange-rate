(ns get-exchange-rate.core
  (:require [clojure.string :as str]
            [clj-http.client :as client]
            [net.cgrand.enlive-html :as html]
            [incanter.core :as ic])
  (:gen-class))

(defn build-url
  "Build the url - params month(MM) and year(yyyy) to get the excel file"
  [month year]
  (str "https://www.bcn.gob.ni/estadisticas/mercados_cambiarios/tipo_cambio/cordoba_dolar/mes.php?mes=" month "&anio=" year))

(defn to-keyword
  "This takes a string and returns a normalized keyword."
  [input]
  (-> input
      str/lower-case
      (str/replace \space \-)
      keyword))

(defn get-snippet
  "Loads the html file from url"
  [url]
  (let [page (client/get url {:insecure? true})]
    (html/html-snippet (:body page))))

(defn load-data
  "Loading the data from a table at the html-snippet"
  [month year]
  (let [url (build-url month year)
        snippet (get-snippet url)
        table (html/select snippet [:table.cuerpo])
        headers (->> (html/select table [:tr])
                     first
                     (#(html/select % [:b]))
                     (map #(html/text %))
                     (map to-keyword)
                     vec)
        rows (->> (html/select table [:tr])
                  rest
                  (map #(html/select % [:div]))
                  (map #(html/texts %))
                  (map #())
                  (filter seq))]
    (ic/dataset headers rows)))

(defn main
  [m y]
  (let [dt (load-data m y)]
    (ic/view dt)))
