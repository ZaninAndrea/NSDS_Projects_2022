FROM busybox:1.35

RUN adduser -D static
USER static
WORKDIR /home/static

COPY ./web/build .

# Run BusyBox httpd
CMD ["busybox", "httpd", "-f", "-v", "-p", "3000"]
