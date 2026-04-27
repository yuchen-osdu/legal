<!--- Deploy -->

# Deploy helm chart

## Introduction

This chart bootstraps a deployment on a [Kubernetes](https://kubernetes.io) cluster using [Helm](https://helm.sh) package manager.

## Prerequisites

The code was tested on **Kubernetes cluster** (v1.21.11) with **Istio** (1.12.6)
  > Istio is installed with Istio Ingress Gateway

- Kubernetes cluster version can be checked with the command:

    `kubectl version --short | grep Server`

    The output will be similar to the following:

  ```console
  Server Version: v1.21.11-gke.1100
  ```

- Istio version can be checked in different ways, it is out of scope for this README. You can find more information [here](https://istio.io/latest/docs/setup/install/).

    The following command shows how to check version if Anthos Service Mesh is used:

    `kubectl -n istio-system get pods -lapp=istiod -o=jsonpath='{.items[0].metadata.labels.istio\.io/rev}'`

    The output will be similar to the following:

  ```console
  asm-1132-5
  ```

> It is possible to use other versions, but it hasn't been tested

This example describes installation in **Development mode**:

- In this mode helm chart is installed to the namespace **not labeled with Istio**.
  > More information about labeling can be found [here](https://istio.io/latest/docs/setup/additional-setup/sidecar-injection) (Istio) or [here](https://cloud.google.com/service-mesh/docs/managed/select-a-release-channel#default-injection-labels) (Anthos Service Mesh)

    You can find all labels for your namespace with the command:

     `kubectl get namespace <namespace> -o jsonpath={.metadata.labels}`

    The output shows that there are no any labels related to Istio:

    ```console
    {"kubernetes.io/metadata.name":"default"}
    ```

    When the namespace is labeled with Istio, the output could be:

    ```console
    {"istio-injection":"enabled","kubernetes.io/metadata.name":"default"}
    ```

### Operation system

The code works in Debian-based Linux (Debian 10 and Ubuntu 20.04) and Windows WSL 2. Also, it works but is not guaranteed in Google Cloud Shell. All other operating systems, including macOS, are not verified and supported.

### Packages

Packages are only needed for installation from a local computer.

- **HELM** (version: v3.7.1 or higher) [helm](https://helm.sh/docs/intro/install/)

    Helm version can be checked with the command:

    `helm version --short`

    The output will be similar to the following:

  ```console
  v3.7.1+gd141386
  ```

- **Kubectl** (version: v1.21.0 or higher) [kubectl](https://kubernetes.io/docs/tasks/tools/#kubectl)

    Kubectl version can be checked with the command:

    `kubectl version --short | grep Client`

    The output will be similar to the following:

  ```console
  Client Version: v1.21.0
  ```

## Installation

First you need to set variables in **values.yaml** file using any code editor. Some of the values are prefilled, but you need to specify some values as well. You can find more information about them below.

### Global variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**global.domain** | your domain for the external endpoint, ex `example.com` | string | - | yes
**global.limitsEnabled** | whether CPU and memory limits are enabled | boolean | true | yes
**global.dataPartitionId** | data partition id | string | - | yes

### Configmap variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**data.logLevel** | logging level | string | ERROR | yes
**data.acceptHttp** | accept Http traffic | string | true | yes
**data.entitlementsHost** | Entitlements host URL | string | <http://entitlements> | yes
**data.defaultLegalTag** | Default legal tag | string | default-data-tag| yes
**data.legalHost** | Legal host URL | string | <http://legal> | yes

### Deployment variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**data.requestsCpu** | amount of requests CPU | string | 10m | yes
**data.requestsMemory** | amount of requests memory| string | 550Mi | yes
**data.limitsCpu** | CPU limit | string | 1 | only if `global.limitsEnabled` is true
**data.limitsMemory** | memory limit | string | 1G | only if `global.limitsEnabled` is true
**data.cronJobServiceAccountName** | name of the service account used in cronjob | string | - | yes
**data.serviceAccountName** | name of your service account | string | legal | yes
**data.imagePullPolicy** | when to pull the image | string | IfNotPresent | yes
**data.image** | path to the image in a registry | string | - | yes

### Configuration variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**conf.configmap** | configmap to be used | string | legal-config | yes
**conf.appName** | name of the app | string | legal | yes
**conf.minioSecretName** | Secret name for minio service | string | legal-minio-secret | yes
**conf.legalS3SecretName** | Secret name for seaweedfs service | string | legal-seaweedfs-secret | yes
**conf.postgresSecretName** | Secret name for postgres service | string | legal-postgres-secret | yes
**conf.rabbitmqSecretName** | Secret name for rabbitmq service | string | rabbitmq-secret | yes
**conf.bootstrapSecretName** | Secret name for cronjob | string | datafier-secret | yes

### Istio variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**istio.proxyCPU** | CPU request for Envoy sidecars | string | `10m` | yes
**istio.proxyCPULimit** | CPU limit for Envoy sidecars | string | `500m` | yes
**istio.proxyMemory** | memory request for Envoy sidecars | string | `100Mi` | yes
**istio.proxyMemoryLimit** | memory limit for Envoy sidecars | string | `512Mi` | yes

### Install the helm chart

Run this command from within this directory:

```console
helm install core-plus-legal-deploy .
```

## Uninstalling the Chart

To uninstall the helm deployment:

```console
helm uninstall core-plus-legal-deploy
```

To delete secrets and PVCs:

```console
kubectl delete secret --all; kubectl delete pvc --all
```

[Move-to-Top](#deploy-helm-chart)
