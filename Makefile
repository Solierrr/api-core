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

# Compose after base.mk (and the stack fragment). Runs the service on the local
# machine from its Docker Hub image, with secrets read from Infisical.
# Requires Docker and `infisical login`. See helps/TRY-LOCAL.md.
LOCAL_SERVICE ?= api-core
LOCAL_SH := $(ORG_SCRIPTS_DIR)/scripts/local.sh
SECRETS_ENV ?= qa
DB ?= remote
OBS ?= 0
BUILD ?= 0
ALL ?= 0
TAG ?=
LOCAL_ENV := SERVICE=$(LOCAL_SERVICE) ENV=$(SECRETS_ENV) DB=$(DB) OBS=$(OBS) BUILD=$(BUILD) ALL=$(ALL) $(if $(TAG),TAG=$(TAG),) $(if $(ENV_FILE),ENV_FILE=$(ENV_FILE),)

.PHONY: up down logs

up: vault-config ## Run the service locally (DB=local for local databases, OBS=1 for Grafana, BUILD=1 for the local image)
	@$(LOCAL_ENV) sh $(LOCAL_SH) up

down: vault-config ## Stop the local service (ALL=1 also stops databases and Grafana)
	@$(LOCAL_ENV) sh $(LOCAL_SH) down

logs: vault-config ## Follow the local service logs
	@$(LOCAL_ENV) sh $(LOCAL_SH) logs

ifneq ($(wildcard Dockerfile),)
.PHONY: docker-build docker-push

docker-build: vault-config ## Build the local image from the Dockerfile
	@$(LOCAL_ENV) sh $(LOCAL_SH) docker-build

docker-push: vault-config ## Push a development image (TAG=dev-name; never latest or a release tag)
	@$(LOCAL_ENV) sh $(LOCAL_SH) docker-push
endif

ifneq ($(wildcard compose.yaml compose.yml docker-compose.yaml docker-compose.yml),)
.PHONY: compose

compose: vault-config ## Run the repository's own compose file
	@$(LOCAL_ENV) sh $(LOCAL_SH) compose
endif
