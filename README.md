# crosswordapp-backend
Java Spring Boot backend application for CrosswordInfinity.com

### Local Development
1. Check out repo and pull latest changes (all work is currently done on master)
2. Make relevant changes (try to keep commits somewhat small and centered around a single feature/bug)
3. Use a descriptive commit message and push  
> git add --all  
> git commit -m "this is a descriptive commit message about what I changed"  
> git push origin master  

### Running the Application Locally
There are multiple options for running locally.
- The simplest is to just use the IDE run button on the CrosswordApplication class (no additional configurations necessary)  
    - just make sure the application.properties file is being used (should be default, unless the 'prod' profile is activated)
- Can also run via mvn on command line:  
    > mvn spring-boot:run  
- If have docker installed, can modify the docker file to build and run locally, but there is no reason to need this.  
-- if do want to build docker image locally, 
modify the ENTRYPOINT line in the Dockerfile by removing the last string in the array that sets the profile to prod.

All of these should expose port 8080 on localhost (as set in application.properties).
The dev properties file will connect to the dev instance of postgres so there is no need to have postgres running locally.