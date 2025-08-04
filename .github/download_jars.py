import requests
import random
import os
import json
import datetime

print('Job Starting')

BASE_URL = "https://search.maven.org/solrsearch/select"
DOWNLOAD_URL_TEMPLATE = "https://repo1.maven.org/maven2/{group}/{artifact}/{version}/{artifact}-{version}.jar"
metadata_path = os.getenv('METADATA_PATH', 'metadata.json')
NUM_JARS = 100
MAX_SIZE_MB = 5 * 1024 * 1024  # 5MB in bytes


def construct_download_url(group, artifact, version):
    group_path = group.replace('.', '/')
    return DOWNLOAD_URL_TEMPLATE.format(group=group_path, artifact=artifact, version=version)


def can_download_file(url):
    response = requests.get(url, stream=True)
    response.raise_for_status()
    total_size = int(response.headers.get('content-length', 0))
    if total_size > MAX_SIZE_MB:
        return False
    else:
        return True


# Function to get a random artifact from Maven Central
def get_random_artifact():
    params = {
        'q': 'p:jar',
        'rows': 1,
        'wt': 'json',
        'start': random.randint(0, 2000000)  # Adjust range for better randomness
    }
    response = requests.get(BASE_URL, params=params)
    response.raise_for_status()
    docs = response.json().get('response', {}).get('docs', [])
    if not docs:
        return None
    return docs[0]


downloaded_count = 0


def get_metadata():
    print("Reading metadata")
    if os.path.isfile(metadata_path):  # Check if it is a file
        with open(metadata_path, 'r') as file:
            return json.load(file)
    elif os.path.isdir(metadata_path):
        raise IsADirectoryError(f"{metadata_path} is a directory, not a file.")
    else:
        print("No metadata file found, so creating new one")
        os.makedirs(os.path.dirname(metadata_path), exist_ok=True)
        # Create the file with an empty array
        with open(metadata_path, 'w') as file:
            json.dump({"jars": []}, file, indent=4)
        return {"jars": []}


def save_metadata(data):
    with open(metadata_path, 'w') as file:
        json.dump(data, file, indent=4)


metadata = get_metadata()
print("metadata gathered")
# Download 100 random JARs
while downloaded_count < NUM_JARS:
    artifact = get_random_artifact()
    if not artifact:
        continue
    group = artifact['g']
    artifact_id = artifact['a']
    version = artifact['latestVersion']
    download_url = construct_download_url(group, artifact_id, version)
    artifact_name = f"{artifact_id}-{version}.jar"
    try:
        if not any(jar['name'] == artifact_name for jar in metadata['jars']):
            if can_download_file(download_url):
                metadata['jars'].append({
                    'name': artifact_name,
                    'download_url': download_url,
                    'date': datetime.date.today().isoformat()
                })
                save_metadata(metadata)
                downloaded_count += 1
            else:
                print(f"Skipped (too large)")
    except requests.RequestException as e:
        print(f"Failed to download {download_url}: {e}")
print(f"Downloaded {downloaded_count} JAR files.")
