FROM eclipse-temurin:latest
COPY ./out/artifacts/Shipping_jar/project5.jar /home/out.jar
CMD ["java","-cp","/home/out.jar", "org.polimi.nsds.project5.ShippingService"]