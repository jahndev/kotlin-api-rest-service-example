service-up-compose = docker-compose -f docker/base.yml
mock-up-compose = docker-compose -f docker/mock.yml

.PHONY: compose.service.up
compose.service.up: build
	$(service-up-compose) build
	$(service-up-compose) up -d

.PHONY: compose.service.down
compose.service.down:
	$(service-up-compose) down


##########################################################
# Handle the deploy on OpenShift
##########################################################

.PHONY: configmap
configmap:
	oc create $(OC_ARGS) configmap payu-config \
	--from-file=config.yaml=$(CONFIG_FILE) --dry-run -o yaml \
	| oc replace $(OC_ARGS) -f -

.PHONY: deploy
deploy:
	oc $(OC_ARGS) rollout latest dc/$(APP); \
	time_until=$$(( $$( date '+%s' ) + 200 )); \
	echo -n "Waiting for oc to become ready: "; \
	until ( \
					( set -e; oc $(OC_ARGS) rollout status dc/$(APP) --watch=false 2>&1| grep -E '(completed|successfully)' ) \
					) >/dev/null 2>&1; do \
		if [ $$time_until -lt $$( date '+%s' ) ]; then \
			echo "  Timed out!"; \
			echo "oc $${OC_ARGS} rollout status dc/$(APP) --watch=false"; \
			oc $(OC_ARGS) rollout status dc/$(APP) --watch=false; \
			echo "exit 1"; \
			exit 1; \
		fi; \
		echo -n '.'; \
		sleep 0.5; \
	done; \
	echo; \
	echo "oc $${OC_ARGS} rollout status dc/$(APP)"; \
	oc $(OC_ARGS) rollout status dc/$(APP)
