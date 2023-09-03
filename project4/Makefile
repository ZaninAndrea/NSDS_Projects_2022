.PHONY: start stop

start: 
	docker-compose up -d
	./spark/sbin/start-master.sh
	./spark/sbin/start-worker.sh spark://127.0.0.1:7077
	./spark/sbin/start-history-server.sh
	open http://127.0.0.1:8080/
	open http://127.0.0.1:18080/

stop:
	docker-compose down
	./spark/sbin/stop-master.sh
	./spark/sbin/stop-worker.sh
	./spark/sbin/stop-history-server.sh
