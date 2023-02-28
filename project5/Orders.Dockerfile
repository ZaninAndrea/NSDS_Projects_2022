FROM eclipse-temurin:latest
COPY ./out/artifacts/Orders_jar/project5.jar /home/out.jar
CMD ["java","-cp","/home/out.jar", "org.polimi.nsds.project5.OrdersService"]