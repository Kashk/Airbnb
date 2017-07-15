DROP TABLE IF EXISTS airbnb2;
CREATE TABLE airbnb2 (
	zipcode int(5) UNSIGNED ZEROFILL NOT NULL,
	average_price int(10) DEFAULT NULL,
	month int(10) NOT NULL,
	year int(10) NOT NULL,
	url varchar(200) NOT NULL,
	crawl_time timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
	PRIMARY KEY (zipcode, crawl_time, month, year)
);

DROP TABLE IF EXISTS airbnb2;
CREATE TABLE airbnb2 (
	zipcode int(5) UNSIGNED ZEROFILL NOT NULL,
	city varchar(50) DEFAULT NULL,
	state varchar(50) DEFAULT NULL,
	average_price int(10) DEFAULT NULL,
	month int(10) NOT NULL,
	year int(10) NOT NULL,
	url varchar(200) NOT NULL,
	crawl_time timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
	PRIMARY KEY (zipcode, city, state, crawl_time, month, year)
);

INSERT INTO airbnb2 SELECT * FROM airbnb;

DELETE a2
FROM airbnb3 as a1
JOIN airbnb3 as a2
ON a1.zipcode = a2.zipcode AND a1.month = a2.month AND a1.year = a2.year AND a1.crawl_time > a2.crawl_time;

DELETE *
FROM fsbo_home_5_2017 as fsbo
WHERE fsbo.crawl.crawl_time > '2017-6-19 00:00:00';

SELECT COUNT(*)
FROM airbnb as a1
JOIN airbnb as a2
ON a1.zipcode = a2.zipcode AND a1.month = a2.month AND a1.year = a2.year AND a1.crawl_time > a2.crawl_time;

SELECT *
FROM cities_extended as c1
JOIN cities_extended as c2
ON c1.zip = c2.zip AND c1.city != c2.city;

select * from table where email in (
    select email from table
    group by email having count(*) > 1
)

SELECT *
FROM cities_extended
WHERE zip IN (
	SELECT zip FROM cities_extended
	GROUP BY zip HAVING COUNT(*) > 1
);

SELECT *
FROM cities_extended
GROUP BY city HAVING COUNT(*) > 1;