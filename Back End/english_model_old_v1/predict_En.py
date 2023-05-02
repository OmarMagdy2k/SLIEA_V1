#!/usr/bin/env python
# coding: utf-8

# In[ ]:


import cv2 
import numpy as np 
import mediapipe as mp 
from keras.models import load_model 

# import sys
# import locale
# import io

model  = load_model("./english_model/model")
label = np.load("./english_model/labels.npy")

holistic = mp.solutions.holistic
hands = mp.solutions.hands
holis = holistic.Holistic()
drawing = mp.solutions.drawing_utils


def predicted_frame(image_path):
    frm=cv2.imread(image_path)
    lst = []

    frm = cv2.flip(frm, 1)

    res = holis.process(cv2.cvtColor(frm, cv2.COLOR_BGR2RGB))

    pred = ''
    if res.face_landmarks:
        for i in res.face_landmarks.landmark:
            lst.append(i.x - res.face_landmarks.landmark[1].x)
            lst.append(i.y - res.face_landmarks.landmark[1].y)

        if res.left_hand_landmarks:
            for i in res.left_hand_landmarks.landmark:
                lst.append(i.x - res.left_hand_landmarks.landmark[8].x)
                lst.append(i.y - res.left_hand_landmarks.landmark[8].y)
        else:
            for i in range(42):
                lst.append(0.0)

        if res.right_hand_landmarks:
            for i in res.right_hand_landmarks.landmark:
                lst.append(i.x - res.right_hand_landmarks.landmark[8].x)
                lst.append(i.y - res.right_hand_landmarks.landmark[8].y)
        else:
            for i in range(42):
                lst.append(0.0)

        lst = np.array(lst).reshape(1,-1)

        pred = label[np.argmax(model.predict(lst))]

    return pred

# image_path="E:\\Uni\\Final Project\\Back End\\images\\image2621101635021160227.jpg"
# print(predicted_frame(image_path))