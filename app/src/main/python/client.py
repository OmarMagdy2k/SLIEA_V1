import requests


def translate_image(file_name):
    s = requests.Session()

    r = s.get("http://192.168.1.5:8000/translate_image/", params={'file_name': file_name})
    return r.json()['prediction']


# print(translate_image('image2621101635021160227.jpg'))
def statement2words_En(statement):
    s = requests.Session()
    r = s.get("http://192.168.1.5:8000/statment2words_En/", params={'statement': statement})
    return r.json()['words']


def statement2words_Ar(statement):
    s = requests.Session()
    r = s.get("http://192.168.1.5:8000/statment2words_Ar/", params={'statement': statement})
    return r.json()['words']


def txt2speech(text):
    s = requests.Session()
    r = s.get("http://192.168.1.5:8000/txt2speech/", params={'text': text})
    return r.json()['voice']
