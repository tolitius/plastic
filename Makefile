.PHONY: clean jar tag test outdated install deploy tree repl

clean:
	rm -rf target
	rm -rf classes

jar: clean tag
	clojure -A:jar

outdated:
	clojure -M:outdated

tag:
	clojure -A:tag

test:
	clojure -X:test :patterns '[".*test" ".*test.schema"]'

install: jar
	clojure -A:install

deploy: jar
	clojure -A:deploy

tree:
	mvn dependency:tree

## WARNING: Use of :main-opts with -A is deprecated. Use -M instead.
## but... does not work with "-M"s ¯\_(ツ)_/¯
repl:
	clojure -A:dev -A:repl
