import requests



def translate_image_En(file_name):
    s = requests.Session()

    r = s.get("http://192.168.1.5:8000/translate_image/", params={'file_name': file_name})
    return r.json()['prediction']

def translate_image_Ar(file_name):
    s = requests.Session()
    r = s.get("http://192.168.1.5:8000/translate_image/", params={'file_name': file_name})
    return r.json()['prediction']

# print(translate_image('image2621101635021160227.jpg'))
