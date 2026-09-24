SHELL := /bin/sh

ORG_SCRIPTS_DIR ?= $(HOME)/.local/share/solierrr-infra-scripts
ORG_SCRIPTS_REPO ?= https://github.com/Solierrr/infra-scripts.git
ORG_SCRIPTS_POWERSHELL ?= powershell
EXTRACT_ENV := $(ORG_SCRIPTS_DIR)/scripts/extract-env.ps1
SERVICE := api-core
ENV ?= local
OUT ?= .env
MVNW := ./mvnw

.DEFAULT_GOAL := help

.PHONY: help vault-config vault-auth extract-env tools-check env setup run build test check clean

help: ## Show the available commands
	@awk 'BEGIN {FS = ":.*## "; printf "Usage: make <target>\\n\\n"} /^[a-zA-Z_-]+:.*## / {printf "  %-16s %s\\n", $$1, $$2}' $(MAKEFILE_LIST)

vault-config: ## Clone or update the shared infra-scripts toolkit
	@if [ -d "$(ORG_SCRIPTS_DIR)/.git" ]; then \
		echo "infra-scripts found at $(ORG_SCRIPTS_DIR), updating..."; \
		git -C "$(ORG_SCRIPTS_DIR)" pull --ff-only || { echo "error: 'git pull --ff-only' failed in $(ORG_SCRIPTS_DIR). Resolve manually, then run 'make vault-config' again."; exit 1; }; \
	elif [ -e "$(ORG_SCRIPTS_DIR)" ]; then \
		echo "error: $(ORG_SCRIPTS_DIR) exists but is not a git clone. Remove or rename it, then run 'make vault-config' again."; exit 1; \
	else \
		echo "infra-scripts not found, cloning into $(ORG_SCRIPTS_DIR)..."; \
		git clone "$(ORG_SCRIPTS_REPO)" "$(ORG_SCRIPTS_DIR)" || { echo "error: failed to clone $(ORG_SCRIPTS_REPO). Check your network/access, then run 'make vault-config' again."; exit 1; }; \
	fi
	@test -f "$(EXTRACT_ENV)" || { echo "error: infra-scripts was cloned/updated but $(EXTRACT_ENV) is missing. Check if the script was renamed or moved upstream."; exit 1; }
	@echo "OK: infra-scripts ready at $(ORG_SCRIPTS_DIR)"

vault-auth: vault-config ## Check the Infisical CLI is installed and authenticated
	@command -v infisical >/dev/null 2>&1 || { echo "error: Infisical CLI not installed. Install it (https://infisical.com/docs/cli/overview), then run 'make vault-auth' again."; exit 1; }
	@infisical user get token --silent >/dev/null 2>&1 || { \
		echo "error: no active Infisical session."; \
		echo "Run: infisical login"; \
		echo "Then run 'make extract-env' again."; \
		exit 1; \
	}
	@echo "OK: Infisical authenticated."

extract-env: vault-auth ## Generate .env from Infisical (ENV=local OUT=.env)
	@test -n "$(SERVICE)" || { echo "error: SERVICE not set. Example: make extract-env SERVICE=api-core"; exit 1; }
	@case "$(ENV)" in local|qa|prod) : ;; *) echo "error: invalid ENV '$(ENV)'. Use local, qa or prod (example: make extract-env ENV=qa)"; exit 1;; esac
	$(ORG_SCRIPTS_POWERSHELL) -NoProfile -ExecutionPolicy Bypass -File "$(EXTRACT_ENV)" -Service "$(SERVICE)" -Environment "$(ENV)" -OutputPath "$(OUT)"

tools-check: vault-config ## Alias for vault-config (kept for backwards compatibility)
env: extract-env ## Alias for extract-env (kept for backwards compatibility)

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
