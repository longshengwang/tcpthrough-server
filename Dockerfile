FROM openjdk:8
RUN mkdir /root/tcpth
WORKDIR /root/tcpth
COPY build/distributions/server-1.0 /root/tcpth
RUN ln -s /root/tcpth/bin/server /usr/sbin/tcpth-server
