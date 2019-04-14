import pandas as pd
import numpy as np

for i in range(4):
    for j in range(2, 8):
        df = pd.read_excel('mark.xls', sheet_name=[i], usecols=[j])[i]
        name = df.columns.values.tolist()[0]
        max_s = df.max().values[0]
        min_s = df.min().values[0]
        mean = df.mean().values[0]
        pass_rate = df[df >= 60].count()[0] / len(df)
        good_rate = df[df >= 90].count()[0] / len(df)
        print(name, max_s, min_s, mean, pass_rate, good_rate)

# data = pd.read_excel('mark.xls', usecols=[0, 1], converters={0: str}).values
# for (student_number, name) in data:
#     print(student_number)
#     user = User(student_number=student_number, password=student_number, name=name)
#     db.session.add(user)
# db.session.commit()


# data = pd.read_excel('mark.xls', sheet_name=[0, 1, 2, 3], usecols=[0, 2, 3, 4, 5, 6, 7], converters={0: str})
# for i in range(4):
#     # print(data[i].describe().values.tolist())
#     name_list = data[i].columns.values.tolist()
#     for chart in data[i].values:
#         student_number = chart[0]
#         print(chart[1:].mean())
#         break

# for j in range(1, 7):
#     print(name_list[j], chart[j], student_number)

# print(data[i].columns.values.tolist())
# print(data[i].values)


# for (student_number, name,) in data[0].values:
#     print(student_number)

# def read_data(path):
#     try:
#         for i in range(10000):
#             data = pd.read_excel('mark.xls', sheet_name=[0, 1, 2, 3], usecols=[0, 1, 2, ], converters={0: str})
#             yield data
#     except IndexError:
#         return
