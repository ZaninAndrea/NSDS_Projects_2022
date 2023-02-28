FROM eclipse-temurin:latest
COPY ./out/artifacts/Items_jar/project5.jar /home/out.jar
CMD ["java","-cp","/home/out.jar", "org.polimi.nsds.project5.ItemsService"]