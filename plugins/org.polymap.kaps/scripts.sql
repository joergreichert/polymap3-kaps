select * from K_EGEB LIMIT 20

select distinct(SANANFEND) from K_EGEB LIMIT 20

select distinct(GLDAUER) from K_EWOHN LIMIT 20


select * from K_EWOHN where objektnr = 1001 and gebnr = 2 and WOHNUNGSNR = 1

select * from K_EWOHN where objektnr = 2018 and gebnr = 1 and WOHNUNGSNR = 14


select * from K_EWOHN where LIEZINS > 0

select * from K_EWOHN where HINWEIS is not null

select * from K_HIMRI