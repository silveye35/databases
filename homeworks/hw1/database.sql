create database silveye_hw1

use silveye_hw1

#creates 'department' relation
create table department
	(dept_name varchar(20),
    building	varchar(15),
    budget		numeric(12,2),
    primary key  (dept_name));
    

#inserts values into the 'department' relation
insert into department values ('Biology','Watson',90000);
insert into department values ('Comp.Sci.','Taylor',100000);
insert into department values ('Elec.Eng.','Taylor',85000);
insert into department values ('Finance','Painter',120000);
insert into department values ('History','Painter',50000);
insert into department values ('Music','Packard',80000);
insert into department values ('Physics','Watson',70000);


#creates the 'course' relation
CREATE TABLE course (
    course_id VARCHAR(7),
    title VARCHAR(50),
    dept_name VARCHAR(20),
    credits NUMERIC(2 , 0 ),
    PRIMARY KEY (course_id),
    FOREIGN KEY (dept_name)
        REFERENCES department (dept_name)
);


#inserts values into the 'course' relation
insert into course values('BIO-101', 'Intro. to Biology', 'Biology', 4);
insert into course values('BIO-301', 'Genetics', 'Biology', 4);
insert into course values('BIO-399', 'Computational Biology', 'Biology', 3);
insert into course values('CS-101', 'Intro. to Computer Science', 'Comp.Sci.', 4);
insert into course values('CS-190', 'Game Design', 'Comp.Sci.', 4);
insert into course values('CS-315', 'Robotics', 'Comp.Sci.', 3);
insert into course values('CS-319', 'Image Processing', 'Comp.Sci.', 3);
insert into course values('CS-347', 'Database System Concepts', 'Comp.Sci.', 3);
insert into course values('EE-181', 'Intro. to Digital Systems', 'Elec.Eng.', 3);
insert into course values('FIN-201', 'Investment Banking', 'Finance', 3);
insert into course values('HIS-351', 'World History', 'History', 3);
insert into course values('MU-199', 'Music Video Production', 'Music', 3);
insert into course values('PHY-101', 'Physical Principles', 'Physics', 4);


#creates the 'instructor' relation
CREATE TABLE instructor (
    ID VARCHAR(5),
    name VARCHAR(20) NOT NULL,
    dept_name VARCHAR(20),
    salary NUMERIC(8 , 2 ),
    PRIMARY KEY (ID),
    FOREIGN KEY (dept_name)
        REFERENCES department (dept_name)
);

#inserts values into the 'instructor' relation
insert into instructor values('10101', 'Srinivasan', 'Comp.Sci.', 65000);
insert into instructor values('12121', 'Wu', 'Finance', 90000);
insert into instructor values('15151', 'Mozart', 'Music', 40000);
insert into instructor values('22222', 'Einstein', 'Physics', 95000);
insert into instructor values('32343', 'El Said', 'History', 60000);
insert into instructor values('33456', 'Gold', 'Physics', 87000);
insert into instructor values('45565', 'Katz', 'Comp.Sci.', 75000);
insert into instructor values('58583', 'Califieri', 'History', 62000);
insert into instructor values('76543', 'Singh', 'Finance', 80000);
insert into instructor values('76766', 'Crick', 'Biology', 72000);
insert into instructor values('83821', 'Brandt', 'Comp.Sci.', 92000);
insert into instructor values('98345', 'Kim', 'Elec.Eng.', 80000);


#creates the 'section' relation
CREATE TABLE section (
    course_id VARCHAR(8),
    sec_id VARCHAR(8),
    semester VARCHAR(8),
    year NUMERIC(4 , 0 ),
    building VARCHAR(15),
    room_number VARCHAR(7),
    time_slot_id VARCHAR(4),
    PRIMARY KEY (course_id , sec_id , semester , year),
    FOREIGN KEY (course_id)
        REFERENCES course (course_id)
);
    

#inserts values into the 'section' relation
insert into section values('BIO-101', '1', 'Summer', 2009, 'Painter', '514', 'B');
insert into section values('BIO-301', '1', 'Summer', 2010, 'Painter', '514', 'A');
insert into section values('CS-101', '1', 'Fall', 2009, 'Packard', '101', 'H');
insert into section values('CS-101', '1', 'Spring', 2010, 'Packard','101', 'F');
insert into section values('CS-190', '1', 'Spring', 2009, 'Taylor', '3128', 'E');
insert into section values('CS-190', '2', 'Spring', 2009, 'Taylor', '3128', 'A');
insert into section values('CS-315', '1', 'Spring', 2010, 'Watson', '120', 'D');
insert into section values('CS-319', '1', 'Spring', 2010, 'Watson', '100', 'B');
insert into section values('CS-319', '2', 'Spring', 2010, 'Taylor', '3128', 'C');
insert into section values('CS-347', '1', 'Fall', 2009, 'Taylor', '3128', 'A');
insert into section values('EE-181', '1', 'Spring', 2009, 'Taylor', '3128', 'C');
insert into section values('FIN-201', '1', 'Spring', 2010, 'Packard', '101', 'B');
insert into section values('HIS-351', '1', 'Spring', 2010, 'Painter', '514', 'C');
insert into section values('MU-199', '1', 'Spring', 2010, 'Packard','101', 'D');
insert into section values('PHY-101', '1', 'Fall', 2009, 'Watson', '100', 'A');


#creates the 'teaches' relation
CREATE TABLE teaches (
    ID VARCHAR(5),
    course_id VARCHAR(8),
    sec_id VARCHAR(8),
    semester VARCHAR(6),
    year NUMERIC(4,0),
    PRIMARY KEY (ID, course_id, sec_id, semester, year),
    FOREIGN KEY (course_id , sec_id , semester , year)
	REFERENCES section(course_id , sec_id , semester , year),
    FOREIGN KEY (ID)
	REFERENCES instructor (ID)
);

#inserts values into the 'teaches' relation
insert into teaches values('10101', 'CS-101', '1', 'Fall', 2009);
insert into teaches values('10101', 'CS-315', '1', 'Spring', 2010);
insert into teaches values('10101', 'CS-347', '1', 'Fall', 2009);
insert into teaches values('12121', 'FIN-201', '1', 'Spring', 2010);
insert into teaches values('15151', 'MU-199', '1', 'Spring', 2010);
insert into teaches values('22222', 'PHY-101', '1', 'Fall', 2009);
insert into teaches values('32343', 'HIS-351', '1', 'Spring', 2010);
insert into teaches values('45565', 'CS-101', '1', 'Spring', 2010);
insert into teaches values('45565', 'CS-319', '1', 'Spring', 2010);
insert into teaches values('76766', 'BIO-101', '1', 'Summer', 2009);
insert into teaches values('76766', 'BIO-301', '1', 'Summer', 2010);
insert into teaches values('83821', 'CS-190', '1', 'Spring', 2009);
insert into teaches values('83821', 'CS-190', '2', 'Spring', 2009);
insert into teaches values('83821', 'CS-319', '2', 'Spring', 2010);
insert into teaches values('98345', 'EE-181', '1', 'Spring', 2009);

#-----Start of queries-------#

#3.a
select distinct(course_id)
from teaches
where (teaches.semester = 'Fall' AND teaches.year = 2009) OR (teaches.semester = 'Spring' AND teaches.year = 2010);

#3.b
select dept_name, avg (salary)
from instructor 
group by dept_name
having avg(salary) > 42000;

#3.c
select ID,name
from instructor
where salary = (select max(salary) from instructor);

#3.d
select dept_name, max(salary)
from instructor
group by dept_name;

SET SQL_SAFE_UPDATES = 0;
#3.e
delete from course
where course_id not in
		(select course_id from section);

select * from course;









    

    

    
    