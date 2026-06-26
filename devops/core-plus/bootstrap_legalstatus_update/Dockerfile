FROM alpine:latest
WORKDIR /opt
RUN apk update && \
    apk add --no-cache bash jq curl ca-certificates
COPY ./devops/core-plus/bootstrap_legalstatus_update/ /opt/
RUN chmod +x update_legal_status.sh validate-env.sh
RUN addgroup -g 10001 -S nonroot \
  && adduser -h /opt -G nonroot -S -u 10001 nonroot
RUN chown -R 10001:10001 /opt
USER 10001:10001
CMD ["/bin/bash", "-c", "./update_legal_status.sh"]
