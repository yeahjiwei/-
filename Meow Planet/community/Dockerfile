FROM openjdk:8
ADD target/ROOT.jar app.jar
CMD java -jar app.jar --spring.thymeleaf.prefix=classpath:/templates/ --spring.thymeleaf.suffix=.html --spring.profiles.active=test