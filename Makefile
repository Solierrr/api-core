ifeq ($(OS),Windows_NT)
ORG_SCRIPTS_DIR ?= $(USERPROFILE)/.local/share/solierrr-infra-scripts
ORG_SCRIPTS_POWERSHELL ?= powershell
else
ORG_SCRIPTS_DIR ?= $(HOME)/.local/share/solierrr-infra-scripts
ORG_SCRIPTS_POWERSHELL ?= pwsh
endif
ORG_SCRIPTS_REPO ?= https://github.com/Solierrr/infra-scripts.git
EXTRACT_ENV := $(ORG_SCRIPTS_DIR)/scripts/extract-env.ps1
SERVICE ?=
ENV ?=
OUT ?= .env


MVNW := ./mvnw

.DEFAULT_GOAL := help

.PHONY: help tools-check env setup run build test check clean vault-config vault-auth extract-env

help: ## Show the available commands
	@awk 'BEGIN {FS = ":.*## "; printf "Usage: make <target>\\n\\n"} /^[a-zA-Z_-]+:.*## / {printf "  %-16s %s\\n", $$1, $$2}' $(MAKEFILE_LIST)



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

vault-config: ## Clone or update the shared infra-scripts toolkit
	$(ORG_SCRIPTS_POWERSHELL) -NoProfile -ExecutionPolicy Bypass -File scripts/make-vault.ps1 -Action config -ScriptsDir "$(ORG_SCRIPTS_DIR)" -Repo "$(ORG_SCRIPTS_REPO)" -ExtractEnvPath "$(EXTRACT_ENV)"

vault-auth: vault-config ## Check that the Infisical CLI is installed and authenticated
	$(ORG_SCRIPTS_POWERSHELL) -NoProfile -ExecutionPolicy Bypass -File scripts/make-vault.ps1 -Action auth

extract-env: vault-auth ## Generate a local environment file; prompts for missing service/environment
	$(ORG_SCRIPTS_POWERSHELL) -NoProfile -ExecutionPolicy Bypass -File scripts/make-vault.ps1 -Action extract-env -ExtractEnvPath "$(EXTRACT_ENV)" -Service "$(SERVICE)" -Environment "$(ENV)" -OutputPath "$(OUT)"

tools-check: vault-config ## Alias for vault-config

env: extract-env ## Alias for extract-env
