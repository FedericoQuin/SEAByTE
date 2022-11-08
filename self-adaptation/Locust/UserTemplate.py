import locust
from typing import Optional, Sequence
import os
import yaml



class UserTemplate(locust.HttpUser):
    # NOTE: make sure to add any newly developed user profiles in the file: 'UserManifest.yml'
    abstract=True
    
    def getRequiredEnvironmentVariables(self) -> Sequence[str]:
        if not hasattr(self, 'requiredVariables'):
            # Read the yaml file to retrieve the required environment variables
            # Throw an error if the entry does not exist in the yaml file
            with open('UserManifest.yml', 'r') as f:
                try:
                    data = yaml.safe_load(f)
                except yaml.YAMLError as exc:
                    raise Exception(f'Could not open or read the user manifest file: {exc}.')
            
                instanceClassName: str = self.__class__.__name__
                if not any(instanceClassName == u['name'] for u in data['users']):
                    raise Exception(f'User profile "{instanceClassName}" is not specified in the user manifest file.')
                
                for userProfile in data['users']:
                    if instanceClassName == userProfile['name']:
                        self.description = userProfile['description']
                        # UserIdLimitA is provided by default when locust users are started from the dashboard
                        self.requiredVariables = userProfile['requiredVariables'] + ['UserIdLimitA']
                        break
            
        return self.requiredVariables
    
    
    def getEnvironmentVariable(self, variableName: str) -> Optional[str]:
        if variableName not in self.getRequiredEnvironmentVariables():
            raise Exception(f'Trying to retrieve environment variable "{variableName}" that was not specified as required.')
        else:
            return os.environ.get(variableName)
