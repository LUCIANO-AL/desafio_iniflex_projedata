create database teste_projedata;

use teste_projedata;

create table tbpessoa(
	id_pessoa int primary key auto_increment,
    nome varchar(50) not null,
    data_nascimento date not null,     
    salario decimal(10,3) not null,
    funcao varchar(50) not null
);

select * from tbpessoa;


insert into tbpessoa(nome, data_nascimento, salario, funcao) values('Maria','2000-10-18','2009.44','Operador');


select nome as Nome, date_format(data_nascimento,'%d/%m/%Y') as Data_Nascimento, funcao as Função, Concat(  
                          Replace  
                           (Replace  
                                 (Replace  
                                  (Format((salario + (salario *10/100)), 3), '.', '|'), ',', '.'), '|', ',')) AS Salario from tbpessoa;
                                  
delete from tbpessoa where id_pessoa = 4;         

select  group_concat(concat(' '),funcao, concat('/'), nome) from tbpessoa;

SELECT * FROM tbpessoa WHERE MONTH(DATA_NASCIMENTO) = 10 or MONTH(DATA_NASCIMENTO) = 12;

select * from tbpessoa order by year(DATA_NASCIMENTO) limit 1;

select nome as Nome, (2024 - year(DATA_NASCIMENTO)) as Idade from tbpessoa order by year(DATA_NASCIMENTO) limit 1;

select Concat(  Replace  (Replace (Replace (Format(sum(salario), 3), '.', '|'), ',', '.'), '|', ',')) AS Salario from tbpessoa;

select nome as Nome, Concat(  Replace  (Replace (Replace (Format((salario /1212), 3), '.', '|'), ',', '.'), '|', ',')) AS Salario from tbpessoa;


             

