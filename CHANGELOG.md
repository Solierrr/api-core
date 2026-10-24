# Changelog

## [3.1.0](https://github.com/Solierrr/api-core/compare/v3.0.1...v3.1.0) (2026-10-24)


### Features

* add bruno api collection ([820e287](https://github.com/Solierrr/api-core/commit/820e287d70a612d16aa1f7bb71b105ccd160d783))
* add collaborative technical projects ([#73](https://github.com/Solierrr/api-core/issues/73)) ([dc177aa](https://github.com/Solierrr/api-core/commit/dc177aa1270b02df87f40210a8a399a55527d6c4))
* add company access codes ([#76](https://github.com/Solierrr/api-core/issues/76)) ([e5da2c5](https://github.com/Solierrr/api-core/commit/e5da2c5388a170cf3446c53ca011e6c7293dbe47))
* add durable offer translation through google-registry ([#74](https://github.com/Solierrr/api-core/issues/74)) ([71f6a5a](https://github.com/Solierrr/api-core/commit/71f6a5a725d1cc5216144d1a92b40aabd90897c8))
* add technician catalog, professional teams and technician approval ([#75](https://github.com/Solierrr/api-core/issues/75)) ([6996f1e](https://github.com/Solierrr/api-core/commit/6996f1eb4225adc9cbcf05f039fba944f5ea5420))
* add the brand catalog and link the models to it ([#70](https://github.com/Solierrr/api-core/issues/70)) ([cf8e146](https://github.com/Solierrr/api-core/commit/cf8e1462f9899937bbad6871fa0d759f062333a2))
* add the endpoints the web app calls and fix the user company 500 ([#81](https://github.com/Solierrr/api-core/issues/81)) ([633752d](https://github.com/Solierrr/api-core/commit/633752d9b0a54dc6ef0cf3a09c3e31b97028cced))
* add the fictional purchase flow with cart, orders, sales and plan ([#68](https://github.com/Solierrr/api-core/issues/68)) ([5edd1bb](https://github.com/Solierrr/api-core/commit/5edd1bb85e08b1a4c3749e3c5104299284b6a8dc))
* add the local run commands ([44d1c0c](https://github.com/Solierrr/api-core/commit/44d1c0cc7690a7005c6b870ce3eb993bf3778273))
* add the offer lifecycle and the public catalog to main ([#71](https://github.com/Solierrr/api-core/issues/71)) ([84362aa](https://github.com/Solierrr/api-core/commit/84362aa9570990cacf6ed868a70949fc02ef678e))
* add the professional onboarding and mobile access to main ([#69](https://github.com/Solierrr/api-core/issues/69)) ([5659f95](https://github.com/Solierrr/api-core/commit/5659f958405cab26a6a4577310d39fe74775dcb7))
* add the public user summary by auth id ([#78](https://github.com/Solierrr/api-core/issues/78)) ([7c62da6](https://github.com/Solierrr/api-core/commit/7c62da658e0dc80fbc898643b8122dd5e5c6fe7a))
* authenticate internal routes with the service key ([#91](https://github.com/Solierrr/api-core/issues/91)) ([6c52392](https://github.com/Solierrr/api-core/commit/6c5239271ef244305fcc4845eb5cc2b18ca7e3cb))
* let the user change the own username ([#79](https://github.com/Solierrr/api-core/issues/79)) ([55f1489](https://github.com/Solierrr/api-core/commit/55f1489a6f1d4622c4f0fe789e560418ae311f90))
* return the owner company logo in the project response ([#88](https://github.com/Solierrr/api-core/issues/88)) ([62c67e2](https://github.com/Solierrr/api-core/commit/62c67e25f838c032cc2365160ff63b3489d16fbf))
* scope resources and add personal mobile service flows ([#66](https://github.com/Solierrr/api-core/issues/66)) ([b5a74a8](https://github.com/Solierrr/api-core/commit/b5a74a86ba3681a297f93582646296b9c86166fd))
* set company type on creation and provision supplier or requester ([#72](https://github.com/Solierrr/api-core/issues/72)) ([21e2920](https://github.com/Solierrr/api-core/commit/21e2920b3c889a58775b3eda8fb3fa0a492468f1))


### Bug Fixes

* build the response dtos by hand to stop the recursion on five get endpoints ([#83](https://github.com/Solierrr/api-core/issues/83)) ([3dd194d](https://github.com/Solierrr/api-core/commit/3dd194d70643fb5482e11eea63204e6471d8b5f1))
* make the api-core work against the real postgres schema ([#77](https://github.com/Solierrr/api-core/issues/77)) ([82397f8](https://github.com/Solierrr/api-core/commit/82397f8b701e8ebbce02c879bc2ddd7b986623ef))

## [3.0.1](https://github.com/Solierrr/api-core/compare/v3.0.0...v3.0.1) (2026-09-30)


### Bug Fixes

* grant pull-requests write permission to release workflow ([54b5122](https://github.com/Solierrr/api-core/commit/54b51224129ad84802a91f3237bc4112e6c7262c))
* pass vault arguments correctly in PowerShell ([0adbfb0](https://github.com/Solierrr/api-core/commit/0adbfb0de8d7bae420624d62344a7385087a159c))
* support powershell secret extraction ([502254b](https://github.com/Solierrr/api-core/commit/502254bb047182df83875a5b8d42287fe5ef2706))

## [3.0.0](https://github.com/Solierrr/api-core/compare/v0.1.0...v3.0.0) (2026-09-24)


### Features

* ([#24](https://github.com/Solierrr/api-core/issues/24)) ([facdb49](https://github.com/Solierrr/api-core/commit/facdb499ddf5b61fe17d8c71002f94b7d486999b))
* * Implement internal user provisioning (M2M) ([#26](https://github.com/Solierrr/api-core/issues/26)) ([56b32c0](https://github.com/Solierrr/api-core/commit/56b32c0cc480ba405e45a848365f7d58150746f5))
* * Implement JWT authentication and service-to-service auth ([#23](https://github.com/Solierrr/api-core/issues/23)) ([d17f462](https://github.com/Solierrr/api-core/commit/d17f4629086cc9832a7ed020f6789c1db4415d80))
* add maven makefile ([#55](https://github.com/Solierrr/api-core/issues/55)) ([56ba77b](https://github.com/Solierrr/api-core/commit/56ba77b383f2f2409025ee75d4e4fcca4eee6d84))
* add supplier search and Redis cache foundation. ([1430fcb](https://github.com/Solierrr/api-core/commit/1430fcbb83200fd971413bd555ec2e3593b28f85))
* adding entity and repository classes from PSQL-BD ([567f073](https://github.com/Solierrr/api-core/commit/567f073c557397a91c506bda757b91e2ecad95bd))
* adding entity and repository classes from PSQL-BD ([#2](https://github.com/Solierrr/api-core/issues/2)) ([43128a1](https://github.com/Solierrr/api-core/commit/43128a1be631f11b1327bdd0701d267f8304f2bd))
* adding pr template ([6dab0fb](https://github.com/Solierrr/api-core/commit/6dab0fb97f1f91ba896754b13242d1e9a83c3e87))
* adding response and request DTOs ([0ef52f3](https://github.com/Solierrr/api-core/commit/0ef52f3def1173368f11b47866ffb683548e9fcb))
* configure Redis and add supplier search foundation. ([6477153](https://github.com/Solierrr/api-core/commit/64771530c5c89123af505fc881f9d128543ba01c))
* configure Upstash Redis and add supplier search filter DTO. ([8120034](https://github.com/Solierrr/api-core/commit/81200348c3848ea043aa89eb1ec2a9b665a0f60d))
* expose supplier search with Redis cache ([05b1a0f](https://github.com/Solierrr/api-core/commit/05b1a0fd8b56e759e6b1a9a5934646621ae083b0))
* implement global exception handling, ProblemDetail factory and standardized error responses ([#6](https://github.com/Solierrr/api-core/issues/6)) ([2db4909](https://github.com/Solierrr/api-core/commit/2db49096ee10b03efc97f59140f95160a7d59167))
* Implement primary API functions layer (domain, DTOs, repositories, services) ([#11](https://github.com/Solierrr/api-core/issues/11)) ([d8029a6](https://github.com/Solierrr/api-core/commit/d8029a6b86b2b9bfce2cdd0175fb2e89be334f8b))
* implement supplier search cache service. ([3db500f](https://github.com/Solierrr/api-core/commit/3db500f676d63350a1dc002e076b153358c6c243))
* implement supplier search cache with Upstash Redis ([43a0125](https://github.com/Solierrr/api-core/commit/43a01259d2f895ca4d95de000a39f9c7e937e8a5))
* Implementing initial Redis project infrastructure. ([99afd44](https://github.com/Solierrr/api-core/commit/99afd44e941f86f9205d40062ed355665765ad94))
* publish docker image on push to main ([#33](https://github.com/Solierrr/api-core/issues/33)) ([975204f](https://github.com/Solierrr/api-core/commit/975204f26c72261355deb4f49a28f4f8fd3c035d))
* wire up qa-sync and repo-cleanup reusable workflows ([#34](https://github.com/Solierrr/api-core/issues/34)) ([e71953a](https://github.com/Solierrr/api-core/commit/e71953a6a7f99a2304197130cc2a56e5a9b33f9d))


### Bug Fixes

* add missing findByAuthId and PermissionRequestDTO fields breaking main build ([#25](https://github.com/Solierrr/api-core/issues/25)) ([dc638ce](https://github.com/Solierrr/api-core/commit/dc638ce945a78a2c4404cca6c82b1d5954a92759))
* corrigir pipeline de CI e SonarQube ([#29](https://github.com/Solierrr/api-core/issues/29)) ([b83bcf1](https://github.com/Solierrr/api-core/commit/b83bcf1f0eb4117c6f13c3ae51bcf68164f83a15))
* move maven-wrapper.properties to .mvn/wrapper ([#22](https://github.com/Solierrr/api-core/issues/22)) ([7879c0d](https://github.com/Solierrr/api-core/commit/7879c0d85134b23775169b4b79a5f214d3ec6891))
* trigger releases by command ([354a8a9](https://github.com/Solierrr/api-core/commit/354a8a953dd073dc0d344008d3700ed2023cda08))
