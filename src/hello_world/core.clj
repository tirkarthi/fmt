(ns hello-world.core
  (:gen-class))

;; http://stackoverflow.com/a/1118788/2610955
(declare justify-length)
(declare default-string)

;; http://stackoverflow.com/a/18940745/2610955
(defn map-longest
  [f default & colls]
  (lazy-seq
   (when (some seq colls)
     (cons
      (apply f (map #(if (seq %) (first %) default) colls))
      (apply map-longest f default (map rest colls))))))

(defn words-seq
  "Split the text into words"
  [text]
  (clojure.string/split text #"\s+"))

(defn gen-range
  "Generates the number of spaces range
  Each word needs to be separated by atleast one space
  Divide number of characters to fill by number of words to get a repeated sequence
  Add 1 from start for the remainder of the division"
  [words justify-length]
  (let [num-words (if (zero? (dec (count words))) 1 (dec (count words)))
        length (reduce + 0 (map count words))
        char-fill (- justify-length length 1)
        initial (repeat num-words (quot char-fill num-words))
        padder  (take num-words (concat (repeat (rem char-fill num-words) 1) (repeat 0)))]
    (concat (map + initial padder) [0])))

(defn pad-spaces
  "Pads the words with the appropriate spaces as per the generated sequence
  "
  [words justify-length]
  (let [space-range (gen-range words justify-length)]
    (apply str (map #(apply str %1 (repeat %2 " ")) words space-range))))

(defn process-file
  "Read line by line to get the space padded lines
  Get all words for the line and paritition them once the sum of characters exceed the justification length
  Helper has coll that collects the partitions
  Word builder keeps track of the words so far for the partition

  doall since for generates lazy seq and relializing a closed stream throws exception
  when doesn't work for (if (test) (do actions) else-actions) -- maybe a macro
  could have used defn- for helper but TIL letfn
  using reductions of the word-seq and length to sum the number of characters so far 
  partition-by with #(quot inc-sum justify-length) by instead of explicit partitioning and mind the stack for large files
  "
  [file]
  (with-open [rdr (clojure.java.io/reader file)]
    (flatten
     (doall
      (for [line (line-seq rdr)]
        (let [coll []
              words (map #(str %1 " ") (clojure.string/split (str line) #"\s+"))
              total 0
              last-total 0]
          (letfn [(helper [coll words total last-total word-builder]
                    (if (not-empty words)
                      (do
                        (let [fword (first words)
                              fword-len (count fword)
                              nwords (next words)]
                          (if (>= total justify-length)
                            (do
                              (helper (conj coll (drop-last word-builder))
                                      nwords
                                      (+ last-total fword-len)
                                      fword-len (conj (into [] (take-last 1 word-builder)) fword))) ;; last doesn't work
                            (helper coll nwords (+ total fword-len) fword-len (conj word-builder fword))
                            )))
                      (conj coll (seq word-builder))))]
            (map #(pad-spaces % justify-length) (helper coll words total last-total [])))))
      ))))

(defn -main
  [& args]
  (let [justify-length (Integer/parseInt (first args))
        files (rest args)]
    (def justify-length justify-length)
    (def default-string (apply str (repeat justify-length " ")))
    ;; Force print with doall
    (doall (apply map-longest println default-string (map process-file files)))))
