
[http://localhost:8080/actuator/prometheus]() 
[http://localhost:9090]( Prometheus ) 
[http://localhost:3000]( Grafana ) 

grafana query 
```
{job="metrics-application-logs"} |= "execution_time" | regexp "duration_sec\":(?P<duration>[0-9\\.]+)" | line_format "{{.duration}}"
```