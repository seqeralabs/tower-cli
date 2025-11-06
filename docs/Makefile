SHELL := /bin/bash

DOCKER ?= squidfunk/mkdocs-material:7.1.2

build:
	./build.sh
	docker run --rm -it -p 8000:8000 -u $(shell id -u) -v ${PWD}/../build:/docs $(DOCKER) build

serve:
	docker run --rm -it -p 8000:8000 -u $(shell id -u) -v ${PWD}/../build:/docs $(DOCKER)
