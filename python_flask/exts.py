from flask_sqlalchemy import SQLAlchemy

db = SQLAlchemy()


def db_commit():
    try:
        db.session.commit()
    except:
        db.session.rollback()
        return False
    else:
        return True


def get_segment(mark):
    if mark >= 90:
        return 0
    elif mark >= 80:
        return 1
    elif mark >= 60:
        return 2
    else:
        return 3
