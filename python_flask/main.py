from config import Config
from exts import db, db_commit, get_segment
from flask import request, jsonify, Flask
from models import User, Mark, Class_mark, FzuClass
from itsdangerous import TimedJSONWebSignatureSerializer as Serializer
import pandas as pd

app = Flask(__name__)
app.config.from_object(Config)
db.init_app(app)


"""
    管理员接口：设置管理员对学生成绩进行管理
    管理员权限：导入成绩文件、创建班级、设置班长、增、删、查、改
    导入成绩：先用upload上传成绩文件、然后调用create接口处理成绩文件
    成绩文件要求：xls、第一列为学号、第二列为姓名、一个学期一张sheet(每张都一样，前两列必须是学号、姓名，然后一个科目一列）
    传入的json：{class_name:班级名(string),
                term_begin:开始的学期序号(int:0-8),
                term_end:结束的学期序号(int:0-8),
                master_number:班长学号} 
"""


def create_rank(**kwargs):
    try:
        class_id = kwargs['class_id']
    except KeyError:
        class_name = request.json.get('class_name')
        class_id = FzuClass.query.filter(FzuClass.name == class_name).first().id
    all_mark = Mark.query.filter(Mark.class_id == class_id)
    subject_name = []
    for mark in all_mark.all():
        subject_name.append(mark.name)
    subject_name = set(subject_name)
    for subject in subject_name:
        subject_mark = all_mark.filter(Mark.name == subject)
        _mark = subject_mark.order_by(-Mark.mark).all()
        rank = 1
        count = len(_mark)
        for mark in _mark:
            mark.rank = rank
            mark.rank_rate = ((rank - 1) / count) * 100
            rank += 1
        _mark = subject_mark.order_by(-Mark.average).all()
        rank = 1
        count = len(_mark)
        for mark in _mark:
            mark.class_rank = rank
            mark.class_rank_rate = ((mark.class_rank - 1) / count) * 100
            rank += 1
    if not db_commit():
        return jsonify({'status': 303}), 303
    return jsonify({'status': 200}), 200


@app.route('/api_1_0/upload', methods=['POST'])
def upload():
    file = request.files.get('file')
    if file:
        file.save('mark.xls')
    else:
        return jsonify({'status': 405})
    return jsonify({'status': 200}), 200


@app.route('/api_1_0/create', methods=['POST'])
def create():
    class_name = request.json.get('class_name')
    term_begin = request.json.get('term_begin')
    term_end = request.json.get('term_end')
    class_master = request.json.get('master_number')

    grades = ['大一上学期', '大一下学期', '大二上学期', '大二下学期', '大三上学期', '大三下学期', '大四上学期', '大四下学期']
    subject_name = []

    # build class
    new_class = FzuClass.query.filter(FzuClass.name == class_name).first()
    if not new_class:
        new_class = FzuClass(name=class_name)
        db.session.add(new_class)
        if not db_commit():
            return jsonify({'status': 303}), 303

    # build student
    data = pd.read_excel('mark.xls', usecols=[0, 1], converters={0: str}).values
    for (student_number, name) in data:
        _user = User.query.filter(User.student_number == student_number).first()
        if _user:
            continue
        else:
            user = User(student_number=student_number, password=student_number, name=name)
            user.class_id = new_class.id
            db.session.add(user)
    if not db_commit():
        return jsonify({'status': 303}), 303

    # build mark
    for i in range(term_end - term_begin + 1):
        data = pd.read_excel('mark.xls', sheet_name=[i], converters={0: str})[i]
        (r, c) = data.shape
        columns = data.columns.values.tolist()
        subject_name.extend(columns[2:])
        for j in range(r):
            user = User.query.filter(User.student_number == data.iloc[j, 0]).first()
            average = float(data.iloc[j, 2:].mean())
            for k in range(2, c):
                mark = Mark(name=columns[k], mark=float(data.iloc[j, k]), user_id=user.id, class_id=new_class.id)
                mark.grade = grades[term_begin + i]
                mark.segment = get_segment(mark.mark)
                mark.average = average
                db.session.add(mark)
        for k in range(2, c):
            df = data.iloc[:, k]
            class_mark = Class_mark(
                name=columns[k],
                max_s=float(df.max()),
                min_s=float(df.min()),
                average=float(df.mean()),
                grade=grades[term_begin + i],
                pass_rate=float(df[df >= 60].count() / len(df) * 100),
                good_rate=float(df[df >= 90].count() / len(df) * 100),
                class_id=new_class.id
            )
            db.session.add(class_mark)
    if not db_commit():
        return jsonify({'status': 303}), 303

    # build rank
    create_rank(class_id=new_class.id)

    # build class master
    set_master(student_number=class_master)
    return jsonify({'status': 200}), 200


@app.route('/api_1_0/master', methods=['POST'])
def set_master(**kwargs):
    try:
        student_number = kwargs['student_number']
    except KeyError:
        student_number = request.json.get('student_number')
    master_user = User.query.filter(User.student_number == student_number).first()
    if master_user:
        master_user.class_master = True
        if not db_commit():
            return jsonify({'status': 303}), 303
    return jsonify({'status': 200}), 200


@app.route('/api_1_0/user', methods=['GET'])
def select():
    student_number = request.json.get('student_number')
    user = User.query.filter(User.student_number == student_number).first()
    _mark = Mark.query.filter(Mark.user_id == user.id).all()
    data = []
    for mark in _mark:
        data.append({"name": mark.name, "mark": mark.mark,
                     "rank": mark.rank})
    return jsonify({"data": data, 'status': 200}), 200


@app.route('/api_1_0/user', methods=['DELETE'])
def delete():
    student_number = request.json.get('student_number')
    user = User.query.filter(User.student_number == student_number).first()
    class_id = user.class_id
    _mark = Mark.query.filter(Mark.user_id == user.id).all()
    for mark in _mark:
        db.session.delete(mark)
    db.session.delete(user)
    if not db_commit():
        return jsonify({'status': 303}), 303
    create_rank(class_id=class_id)
    return jsonify({'status': 200}), 200


@app.route('/api_1_0/user', methods=['PUT'])
def change():
    student_number = request.json.get('student_number')
    subject = request.json.get('subject_name')
    grade = request.json.get('mark')
    user = User.query.filter(User.student_number == student_number).first()
    all_mark = Mark.query.filter(Mark.user_id == user.id)
    subject_mark = all_mark.filter(Mark.name == subject).first()
    subject_mark.mark = grade
    subject_mark.segment = get_segment(grade)
    _mark = all_mark.filter(Mark.grade == subject_mark.grade).all()
    count = len(_mark)
    average = 0
    for mark in _mark:
        average += mark.mark
    for mark in _mark:
        mark.average = average / count
    if not db_commit():
        return jsonify({'status': 303}), 303
    create_rank(class_id=user.class_id)
    return jsonify({'status': 200}), 200


"""
    前端接口： login、get_info、get_class_mark、get_mark
"""


@app.route('/api_1_0/login', methods=['GET'])
def login():
    student_number = request.json.get('student_number')
    password = request.json.get('password')
    user = User.query.filter(User.student_number == student_number).first()
    if user:
        if user.password == password:
            grades = ['大一上学期', '大一下学期', '大二上学期', '大二下学期', '大三上学期', '大三下学期', '大四上学期', '大四下学期']
            all_term = Mark.query.filter(Mark.user_id == user.id)
            ranks = []
            for grade in grades:
                current_term = all_term.filter(Mark.grade == grade).first()
                if current_term:
                    ranks.append(current_term.class_rank)
            token = generate_auth_token(user.id)
            pr_rank = 0
            for rank in ranks:
                pr_rank += rank
            pr_rank = pr_rank // len(ranks)
            return jsonify({'status': 200, 'token': token, 'rank1': ranks[0], 'rank2': ranks[1], 'rank3': ranks[2],
                            'rank4': ranks[3], 'rank5': pr_rank}), 200
        else:
            return jsonify({'status': 203}), 203
    else:
        return jsonify({'status': 401}), 401


@app.route('/api_1_0/info', methods=['GET'])
def get_info():
    token = request.json.get('token')
    user = verify_auth_token(token)
    if user:
        fzuclass = FzuClass.query.filter(FzuClass.id == user.class_id).first()
        term = request.json.get('term') - 1
        grades = ['大一上学期', '大一下学期', '大二上学期', '大二下学期', '大三上学期', '大三下学期', '大四上学期', '大四下学期']
        all_term = Mark.query.filter(Mark.user_id == user.id)
        current_term = all_term.filter(Mark.grade == grades[term])
        current_term_rank = current_term.first().class_rank
        if term == 0:
            compare = 0
        else:
            last_term = all_term.filter(Mark.grade == grades[term - 1]).first()
            compare = last_term.class_rank - current_term_rank
        cnt = current_term.count()
        rate1 = current_term.filter(Mark.segment != 3).count() / cnt * 100
        rate3 = current_term.filter(Mark.segment == 0).count() / cnt * 100
        cnt = all_term.count()
        rate2 = all_term.filter(Mark.segment != 3).count() / cnt * 100
        rate4 = all_term.filter(Mark.segment == 0).count() / cnt * 100
        return jsonify(
            {'status': 200, 'name': user.name, 'rank': current_term_rank,
             'average': round(current_term.first().average, 2),
             'compare': compare, 'rate1': int(rate1), 'rate2': int(rate2),
             'rate3': int(rate3), 'rate4': int(rate4), "class": fzuclass.name}), 200
    else:
        return jsonify({'status': 401}), 401


@app.route('/api_1_0/mark', methods=['GET'])
def get_mark():
    token = request.json.get('token')
    user = verify_auth_token(token)
    if user:
        term = request.json.get('term') - 1
        grades = ['大一上学期', '大一下学期', '大二上学期', '大二下学期', '大三上学期', '大三下学期', '大四上学期', '大四下学期']
        current_term = Mark.query.filter(Mark.user_id == user.id, Mark.grade == grades[term]).all()
        data = []
        for mark in current_term:
            class_mark = Class_mark.query.filter(Class_mark.class_id == user.class_id,
                                                 Class_mark.name == mark.name).first()
            data.append(
                {"name": mark.name, "grade": mark.mark, "segment": mark.segment,
                 "average": round(class_mark.average, 2),
                 "rank": mark.rank, "rank_rate": round(100 - mark.rank_rate, 2)})
        return jsonify(data), 200
    else:
        return jsonify({'status': 401}), 401


@app.route('/api_1_0/class_mark', methods=['GET'])
def get_class_mark():
    token = request.json.get('token')
    user = verify_auth_token(token)
    if user:
        if user.class_master:
            name = request.json.get('subject_name')
            _mark = Mark.query.filter(Mark.name == name, Mark.class_id == user.class_id).order_by(Mark.user_id).all()
            _user = User.query.filter(User.class_id == user.class_id).order_by(User.id).all()
            data = []
            for (mark, user) in zip(_mark, _user):
                mark_dick = {user.name: mark.mark}
                data.append(mark_dick)
            return jsonify({'status': 200, "mark": data}), 200
        else:
            return jsonify({'status': 205}), 205
    else:
        return jsonify({'status': 401}), 401


@app.route('/api_1_0/class_info', methods=['GET'])
def get_class_info():
    token = request.json.get('token')
    user = verify_auth_token(token)
    if user:
        if user.class_master:
            term = request.json.get('term') - 1
            grades = ['大一上学期', '大一下学期', '大二上学期', '大二下学期', '大三上学期', '大三下学期', '大四上学期', '大四下学期']
            _class_mark = Class_mark.query.filter(Class_mark.class_id == user.class_id,
                                                  Class_mark.grade == grades[term]).all()
            data = []
            for class_mark in _class_mark:
                mark_dick = {"name": class_mark.name, "good_rate": round(class_mark.good_rate, 1),
                             "pass_rate": round(class_mark.pass_rate, 1), "average": round(class_mark.average, 2),
                             "max": class_mark.max_s, "min": class_mark.min_s}
                data.append(mark_dick)
            return jsonify(data), 200
        else:
            return jsonify({'status': 205}), 205
    else:
        return jsonify({'status': 401}), 401


def generate_auth_token(user_id, expiration=3600 * 24):
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
