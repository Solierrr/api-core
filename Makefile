SHELL := /bin/sh

ORG_SCRIPTS_DIR ?= $(HOME)/.local/share/solierrr-infra-scripts
ORG_SCRIPTS_POWERSHELL ?= powershell
EXTRACT_ENV := $(ORG_SCRIPTS_DIR)/scripts/extract-env.ps1
SERVICE := api-core
ENV ?= local
OUT ?= .env
MVNW := ./mvnw

.DEFAULT_GOAL := help

.PHONY: help tools-check env setup run build test check clean

help: ## Show the available commands
	@awk 'BEGIN {FS = ":.*## "; printf "Usage: make <target>\\n\\n"} /^[a-zA-Z_-]+:.*## / {printf "  %-16s %s\\n", $$1, $$2}' $(MAKEFILE_LIST)

tools-check: ## Verify that the shared organization scripts are installed
	@test -f "$(EXTRACT_ENV)" || { echo "error: infra-scripts was not found at $(ORG_SCRIPTS_DIR)"; exit 1; }

env: tools-check ## Generate .env from Infisical (ENV=local OUT=.env)
	$(ORG_SCRIPTS_POWERSHELL) -NoProfile -ExecutionPolicy Bypass -File "$(EXTRACT_ENV)" -Service "$(SERVICE)" -Environment "$(ENV)" -OutputPath "$(OUT)"

setup: ## Download Maven dependencies through the wrapper
	$(MVNW) dependency:go-offline

run: ## Start the Spring Boot application
	$(MVNW) spring-boot:run

build: ## Build the application
	$(MVNW) package

test: ## Run automated tests
	$(MVNW) test

check: test ## Run the local validation suite

clean: ## Remove Maven build artifacts
	$(MVNW) clean
