# Ledger build orchestration.
#
# `make build` is the entry point: it builds the React frontend, stages the
# bundle into src/main/resources/frontend, and packages the plugin jar. The
# frontend resources are generated (gitignored); without this step the plugin
# fails at startup with "Cannot find resource at 'frontend'".

MVN   ?= mvn
SHELL := /bin/bash

RESOURCES_FRONTEND := src/main/resources/frontend

.DEFAULT_GOAL := build
.PHONY: build frontend package clean clean-frontend help

## build: Build the frontend and package the plugin jar (default target)
build: frontend package

## frontend: Build the React app and stage it into Maven resources
frontend:
	./scripts/build-frontend.sh

## package: Package the plugin jar (expects a staged frontend)
package:
	$(MVN) clean package

## clean: Remove Maven and frontend build artifacts
clean: clean-frontend
	$(MVN) clean
	rm -rf frontend/build

## clean-frontend: Remove the staged frontend bundle from resources
clean-frontend:
	rm -rf $(RESOURCES_FRONTEND)

## help: List available targets
help:
	@grep -E '^## ' $(MAKEFILE_LIST) | sed 's/## /  /'
