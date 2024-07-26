import requests
import random
import os

print('Job Starting')

BASE_URL = "https://search.maven.org/solrsearch/select"
DOWNLOAD_URL_TEMPLATE = "https://repo1.maven.org/maven2/{group}/{artifact}/{version}/{artifact}-{version}.jar"
OUTPUT_DIR = "downloaded_jars"
NUM_JARS = 1
MAX_SIZE_MB = 5 * 1024 * 1024  # 5MB in bytes

# Ensure output directory exists
if not os.path.exists(OUTPUT_DIR):
    os.makedirs(OUTPUT_DIR)


def construct_download_url(group, artifact, version):
    group_path = group.replace('.', '/')
    return DOWNLOAD_URL_TEMPLATE.format(group=group_path, artifact=artifact, version=version)

def download_file(url, output_path):
    response = requests.get(url, stream=True)
    response.raise_for_status()
    total_size = int(response.headers.get('content-length', 0))
    if total_size > MAX_SIZE_MB:
        return False
    with open(output_path, 'wb') as file:
        for data in response.iter_content(1024):
            file.write(data)
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


# Directory to store downloaded JARs
os.makedirs('jars', exist_ok=True)

downloaded_count = 0
# Download 100 random JARs
while downloaded_count < NUM_JARS:
    artifact = get_random_artifact()
    if not artifact:
        continue
    group = artifact['g']
    artifact_id = artifact['a']
    version = artifact['latestVersion']
    download_url = construct_download_url(group, artifact_id, version)
    output_path = os.path.join(OUTPUT_DIR, f"{artifact_id}-{version}.jar")
    try:
        if download_file(download_url, output_path):
            print(f"Downloaded: {output_path}")
            downloaded_count += 1
        else:
            print(f"Skipped (too large): {output_path}")
    except requests.RequestException as e:
        print(f"Failed to download {download_url}: {e}")
print(f"Downloaded {downloaded_count} JAR files.")
