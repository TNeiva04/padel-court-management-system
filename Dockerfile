#FROM openjdk:21
FROM openjdk:22
WORKDIR /usr/app
COPY ./static-content ./static-content
COPY ./build/libs ./libs
COPY ./build/libs/2425-2-LEIC43D-G07.jar .
CMD ["java", "-jar", "./libs/2425-2-LEIC43D-G07.jar"]
#COPY ./build/libs/LS.jar .
#CMD ["java", "-jar", "./LS.jar"]