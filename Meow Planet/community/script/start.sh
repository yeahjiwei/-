#!bin/bash
app_name='community'
docker stop ${app_name}
echo '----stop container----'
docker rm ${app_name}
echo '----rm container----'
docker run -p 8080:8080 --name ${app_name} \
-v /etc/localtime:/etc/localtime \
-v /home/app/${app_name}/logs:/tmp/logs \
-d xxxx/${app_name}:0.0.1-SNAPSHOT
echo '----start container----'