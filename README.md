# Smartcloud instance prices API

## Assumptions
- Every Prices API call will contain one kind only
- No need to any processing or format transform for timestamp. Hence, using string to handle timestamp is not an issue at this moment.   
- This application is running on Linux environment, "DynamicClassLoadingException: Failed to instantiate type org.fusesource.jansi.WindowsAnsiOutputStream" will be show if running on Windows.
- No security handle on SmartCloud token save in repository directly.

## Design Decisions
- Project is running on scala-sdk-2.13.14 & JDK 17
- When calling prices API, "Request Parameter \[kind\] is mandatory" will be show if missing the request parameter
- When requesting a kind, which cannot be find in SmartCloud, prices API will return 404 error. Beside this situation, all failure will return 500.
- Only unit test has been implemented.

## Run
1. please check the SmartCloud config is correct (by default the SmartCloud is started at localhost:9999) 
2. for running this application:
```
sbt run
```
The API should be running on your port 8080.

## API
[GET] /prices?kind={kind}

Return:
```
{
    kind: String,
    price: Double,
    timestamp: String
}
```

[GET] /instance-kinds

Return:
```
[{
    kind: String
}]
```
