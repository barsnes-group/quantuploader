-- THIS SQL IS INTENDED FOR DATA LOADING, SO IT RUNS AFTER THE SCHEMA AND TABLES ARE CREATED.

-- TODO: diseasescategory enum should be removed in order to use always just this info:

INSERT IGNORE INTO `disease_groups` 
VALUES ('AD','Alzheimer\'s disease',1),('ALS','Amyotrophic Lateral Sclerosis',2),
('MS','Multiple sclerosis',3),('PD','Parkinson\'s disease',4);

INSERT IGNORE INTO `disease_groups_alias` 
VALUES ('AD',1,1),
('PD',2,4),
('Parkinson',3,4),
('ALS',4,2),
('MS',5,3);


INSERT IGNORE INTO `diseases` 
VALUES ('AD','Alzheimer\'s disease',1,1),
('AD dementia\r\n','Alzheimer\'s disease dementia',2,1),
('Aged healthy','Aged healthy',3,1),
('Aged non-AD','Aged non-Alzheimer\'s disease',4,1),
('ALS','Amyotrophic Lateral Sclerosis',5,2),
('CDMS','Clinically definite multiple sclerosis',6,3),
('CIS','Clinically isolated syndrome',7,3),
('CIS-CIS','Clinically isolated syndrome, without conversion to multiple sclerosis',8,3),
('CIS-MS','Clinically isolated syndrome, with conversion to multiple sclerosis',9,3),
('CIS-MS(CIS)','Clinically isolated syndrome, with and without conversion to multiple sclerosis included',10,3),
('Cogn. Healthy','Cognitively healthy',11,1),
('FTD','Frontotemporal dementia',12,1),
('Healthy_AD','Healthy',13,1),
('LBD','Lewy body dementia',14,1),
('MCI','Mild cognitive impairment',15,1),
('MCI nonprogressors','Mild cognitive impairment, without conversion to Alzheimer\'s disease',16,1),
('MCI progressors','Mild cognitive impairment, with conversion to Alzheimer\'s disease',17,1),
('MCI-AD','Mild cognitive impairment, with conversion to Alzheimer\'s disease',18,1),
('MCI-MCI','Mild cognitive impairment, without conversion to Alzheimer\'s disease',19,1),
('Ment. Healthy','Mentally Healthy',20,1),
('MS','Multiple sclerosis',21,3),
('MS treated','Treated multiple sclerosis',22,3),
('NDC','Non-demented controls',23,1),
('Neurol. healthy ','Neurologically healthy controls',24,3),
('Neurological','Neurological conditions (e.g. OND, OIND, Non-MS)',25,3),
('Non AD','Non Alzheimer\'s disease',26,1),
('Non MS','Non multiple sclerosis',27,3),
('Non-AD ','Non Alzheimer\'s disease',28,1),
('Non-AD, healthy ex. ','Non Alzheimer\'s disease, healthy excluded ',29,1),
('Non-demented ','Non-demented controls ',30,1),
('Non-MS ','Non multiple sclerosis ',31,3),
('Non-neurodeg.','Non-neurodegenerative disorders',32,1),
('OIND','Other inflammatory neurological disorders',33,3),
('OIND + OND','Other inflammatory neurological disorders + other neurological disorders',34,3),
('OND','Other neurological disorders',35,2),
('PD','Parkinson\'s disease',36,4),
('PDD','Parkinsons\'s disease dementia',37,4),
('PMS','Progressive multiple sclerosis',38,3),
('PPMS ','Primary-progressive multiple sclerosis ',39,3),
('Preclinical AD ','Preclinical Alzheimer\'s disease ',40,1),
('Prodomal AD ','Prodomal Alzheimer\'s disease ',41,1),
('Progressive MS','Progressive multiple sclerosis',42,3),
('RRMS','Relapsing-remitting multiple sclerosis',43,3),
('RRMS + CIS ','Relapsing-remitting multiple sclerosis + clinically isolated syndrome ',44,3),
('RRMS Nataliz.','Relapsing-remitting multiple sclerosis after Natalizumab treatment',45,3),
('sALS ','Sporadic Amyotrophic Lateral Sclerosis ',46,2),
('SPMS','Secondary-progressive multiple sclerosis',47,3),
('SPMS Lamotri.','Secondary-progressive multiple sclerosis after Lamotrigine treatment',48,3),
('Sympt. Controls','Symptomatic controls',49,3),
('Healthy_ALS','Healthy',50,2),
('Healthy_MS','Healthy',51,3),
('Healthy_PD','Healthy',52,4);