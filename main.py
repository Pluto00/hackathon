from config import Config
from exts import db
from flask import request, session, jsonify, Flask, abort
from models import User, Mark, Class_mark
from itsdangerous import TimedJSONWebSignatureSerializer as Serializer
import pandas as pd

app = Flask(__name__)
app.config.from_object(Config)
db.init_app(app)

@app.route('/api_1_0/create_class_mark', methods=['GET'])
def create_class_mark():
    grade = ['大一上', '大一下', '大二上', '大二下']
    for i in range(4):
        for j in range(2, 8):
            df = pd.read_excel('mark.xls', sheet_name=[i], usecols=[j])[i]
            name = df.columns.values.tolist()[0]
            max_s = float(df.max().values[0])
            min_s = float(df.min().values[0])
            mean = float(df.mean().values[0])
            pass_rate = float(df[df >= 60].count()[0] / len(df) * 100)
            good_rate = float(df[df >= 90].count()[0] / len(df) * 100)
            class_mark = Class_mark(
                name=name,
                max_s=max_s,
                min_s=min_s,
                mean=mean,
                pass_rate=pass_rate,
                good_rate=good_rate
            )
            class_mark.grade = grade[i]
            db.session.add(class_mark)
    db.session.commit()
    return jsonify({'result': 1})


@app.route('/api_1_0/set_master', methods=['POST'])
def set_master():
    student_number = request.json.get('student_number')
    user = User.query.filter(User.student_number == student_number).first()
    if user:
        user.class_master = True
        db.session.commit()
        return jsonify({'result': 1})
    else:
        return jsonify({'result': 0})


@app.route('/api_1_0/create_rank', methods=['GET'])
def create_rank():
    data = pd.read_excel('mark.xls', sheet_name=[0, 1, 2, 3], usecols=[0, 2, 3, 4, 5, 6, 7], converters={0: str})
    grade = ['大一上', '大一下', '大二上', '大二下']
    for i in range(4):
        name_list = data[i].columns.values.tolist()
        for j in range(1, 7):
            rank = 1
            _mark = Mark.query.filter(Mark.name == name_list[j]).order_by(-Mark.mark).all()
            count = len(_mark)
            for mark in _mark:
                mark.rank = rank
                mark.rank_rate = ((rank - 1) / count) * 100
                rank += 1
        _mark = Mark.query.filter(Mark.grade == grade[i]).order_by(-Mark.mean).all()
        rank = 1
        for mark in _mark:
            mark.class_rank = (rank - 1) // 6 + 1
            rank += 1
        db.session.commit()
    return jsonify({'result': 1})


@app.route('/api_1_0/create_student', methods=['GET'])
def create_student():
    data = pd.read_excel('mark.xls', usecols=[0, 1], converters={0: str}).values
    for (student_number, name) in data:
        user = User(student_number=student_number, password=student_number, name=name)
        db.session.add(user)
    db.session.commit()
    return jsonify({'result': 1})


@app.route('/api_1_0/create_mark', methods=['GET'])
def create_mark():
    grade = ['大一上', '大一下', '大二上', '大二下']
    data = pd.read_excel('mark.xls', sheet_name=[0, 1, 2, 3], usecols=[0, 2, 3, 4, 5, 6, 7], converters={0: str})
    for i in range(4):
        name_list = data[i].columns.values.tolist()
        for chart in data[i].values:
            student_number = chart[0]
            user = User.query.filter(User.student_number == student_number).first()
            if user:
                for j in range(1, 7):
                    mark = Mark(name=name_list[j], mark=chart[j])
                    mark.user_id = user.id
                    mark.user = user
                    mark.grade = grade[i]
                    mark.mean = chart[1:].mean()
                    db.session.add(mark)
            else:
                return jsonify({'result': 0})
    db.session.commit()
    return jsonify({'result': 1})


"""------------------------------------------------------------------------------------------------------------------"""


@app.route('/api_1_0/login', methods=['POST'])
def login():
    student_number = request.json.get('student_number')
    password = request.json.get('password')
    user = User.query.filter(User.student_number == student_number).first()
    if user:
        if user.password == password:
            data = {'name': user.name, 'student_number': user.student_number, 'avatar': user.get_avatar()}
            token = generate_auth_token(user.id)
            return jsonify({'data': data, 'token': token}), 200
        else:
            return jsonify({'result': 0}), 203
    else:
        return jsonify({'result': 0}), 401


@app.route('/api_1_0/get_mark', methods=['POST'])
def get_mark():
    token = request.json.get('token')
    user = verify_auth_token(token)
    if user:
        _grade = ['大一上', '大一下', '大二上', '大二下']
        data = {}
        for grade in _grade:
            _mark = Mark.query.filter(Mark.user_id == user.id, Mark.grade == grade).all()
            _data = {}
            for mark in _mark:
                mark_dict = {mark.name: {"mark": mark.mark, "rank": mark.rank,
                                         "rank_rate": mark.rank_rate}}
                _data.update(mark_dict)
            _data.update({"class_rank": _mark[0].class_rank, "mean": _mark[0].mean})
            _data = {grade: _data}
            data.update(_data)
        return jsonify({'data': data}), 200
    else:
        return jsonify({'result': 0}), 401


@app.route('/api_1_0/get_class_mark', methods=['POST'])
def get_class_mark():
    token = request.json.get('token')
    user = verify_auth_token(token)
    if user:
        if user.class_master:
            name = request.json.get('subject_name')
            _mark = Mark.query.filter(Mark.name == name).order_by(-Mark.mark).all()
            data = []
            for mark in _mark:
                mark_dick = {mark.user.name: mark.mark}
                data.append(mark_dick)
            return jsonify({"mark": data}), 200
        else:
            return jsonify({'result': 0}), 205
    else:
        return jsonify({'result': 0}), 401


def generate_auth_token(user_id, expiration=3600):
    s = Serializer(app.config['SECRET_KEY'], expires_in=expiration)
    return str(s.dumps({'id': user_id}), encoding='utf8')


def verify_auth_token(token):
    s = Serializer(app.config['SECRET_KEY'])
    try:
        data = s.loads(token)
    except:
        return None
    user = User.query.filter(User.id == data['id']).first()
    return user


if __name__ == '__main__':
    app.run(debug=True)
