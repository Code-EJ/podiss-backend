import requests
from bs4 import BeautifulSoup
import sys
import json

def get_youtube_video_details(video_url):
    response = requests.get(video_url)
    if response.status_code == 200:
        soup = BeautifulSoup(response.content, 'html.parser')
        
        # Extrair o título do vídeo
        title = soup.find('meta', {'name': 'title'})['content']
        
        # Extrair a descrição do vídeo
        description = soup.find('meta', {'name': 'description'})['content']
        
        # Extrair a URL da miniatura
        thumbnail_url = soup.find('link', {'rel': 'image_src'})['href']
        
        return {
            'title': title,
            'description': description,
            'thumbnail': thumbnail_url
        }
    else:
        return None

# Exemplo de uso
if __name__ == "__main__":
    video_url = sys.argv[1]
    details = get_youtube_video_details(video_url)
    print(json.dumps(details))