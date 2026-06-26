FROM google/cloud-sdk:465.0.0-slim

WORKDIR /opt

RUN apt-get update && apt-get install -yq jq

COPY ./devops/gc/bootstrap_legalstatus_update/ /opt/

RUN chmod +x update_legal_status.sh validate-env.sh

RUN groupadd -g 10001 -r nonroot \
  && useradd -d /opt -g 10001 -r -u 10001 nonroot

RUN chown -R 10001:10001 /opt

USER 10001:10001

CMD ["/bin/bash", "-c", "./update_legal_status.sh"]
