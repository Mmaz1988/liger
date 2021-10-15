% -*- coding: utf-8 -*-

fstructure('Every man loves a woman.',
	% Properties:
	[
	'sentence_id'('S1'),
	'markup_free_sentence'('Every man loves a woman.'),
	'xle_version'('XLE version 2.6.5 (built Sep 16, 2009 16:07 -0700)'),
	'grammar'('/users/red_queen/Desktop/AKR-Semantics/SVN/english-asker-2007-10-23/grammar/english.lfg'),
	'grammar_date'('Jun 30, 2015 14:56'),
	'word_count'('5'),
	'statistics'('1 solutions, 0.030 CPU seconds, 0.000MB max mem, 115 subtrees unified'),
	'rootcategory'('ROOT'),
	'hostname'('red-queen')
	],
	% Choices:
	[
	
	],
	% Equivalences:
	[
	
	],
	% Constraints:
	[
	cf(1,eq(attr(var(0),'PRED'),semform('love',2,[var(8),var(2)],[]))),
	cf(1,eq(attr(var(0),'SUBJ'),var(8))),
	cf(1,eq(attr(var(0),'OBJ'),var(2))),
	cf(1,eq(attr(var(0),'CHECK'),var(1))),
	cf(1,eq(attr(var(0),'TNS-ASP'),var(14))),
	cf(1,eq(attr(var(0),'CLAUSE-TYPE'),'decl')),
	cf(1,eq(attr(var(0),'PASSIVE'),'-')),
	cf(1,eq(attr(var(0),'VTYPE'),'main')),
	cf(1,eq(attr(var(8),'PRED'),semform('man',1,[],[]))),
	cf(1,eq(attr(var(8),'CHECK'),var(9))),
	cf(1,eq(attr(var(8),'NTYPE'),var(10))),
	cf(1,eq(attr(var(8),'SPEC'),var(12))),
	cf(1,eq(attr(var(8),'CASE'),'nom')),
	cf(1,eq(attr(var(8),'NUM'),'sg')),
	cf(1,eq(attr(var(8),'PERS'),'3')),
	cf(1,eq(attr(var(9),'_LEX-SOURCE'),'countnoun-lex')),
	cf(1,eq(attr(var(10),'NSEM'),var(11))),
	cf(1,eq(attr(var(10),'NSYN'),'common')),
	cf(1,eq(attr(var(11),'COMMON'),'count')),
	cf(1,eq(attr(var(12),'QUANT'),var(13))),
	cf(1,eq(attr(var(13),'PRED'),semform('every',0,[],[]))),
	cf(1,eq(attr(var(2),'PRED'),semform('woman',7,[],[]))),
	cf(1,eq(attr(var(2),'CHECK'),var(3))),
	cf(1,eq(attr(var(2),'NTYPE'),var(4))),
	cf(1,eq(attr(var(2),'SPEC'),var(6))),
	cf(1,eq(attr(var(2),'CASE'),'obl')),
	cf(1,eq(attr(var(2),'NUM'),'sg')),
	cf(1,eq(attr(var(2),'PERS'),'3')),
	cf(1,eq(attr(var(3),'_LEX-SOURCE'),'countnoun-lex')),
	cf(1,eq(attr(var(4),'NSEM'),var(5))),
	cf(1,eq(attr(var(4),'NSYN'),'common')),
	cf(1,eq(attr(var(5),'COMMON'),'count')),
	cf(1,eq(attr(var(6),'DET'),var(7))),
	cf(1,eq(attr(var(7),'PRED'),semform('a',5,[],[]))),
	cf(1,eq(attr(var(7),'DET-TYPE'),'indef')),
	cf(1,eq(attr(var(1),'_SUBCAT-FRAME'),'V-SUBJ-OBJ')),
	cf(1,eq(attr(var(14),'MOOD'),'indicative')),
	cf(1,eq(attr(var(14),'PERF'),'-_')),
	cf(1,eq(attr(var(14),'PROG'),'-_')),
	cf(1,eq(attr(var(14),'TENSE'),'pres')),
	cf(1,eq(proj(var(15),'o::'),var(16))),
	cf(1,in_set('ClausePunct',var(16))),
	cf(1,in_set('GenGoodPunct',var(16)))
	],
	% C-Structure:
	[
	cf(1,subtree(1540,'ROOT',1539,199)),
	cf(1,phi(1540,var(0))),
	cf(1,cproj(1540,var(15))),
	cf(1,subtree(1933,'S[fin]',-,1916)),
	cf(1,phi(1933,var(0))),
	cf(1,subtree(1916,'NP',371,1903)),
	cf(1,phi(1916,var(8))),
	cf(1,subtree(1903,'NPadj',-,1693)),
	cf(1,phi(1903,var(8))),
	cf(1,subtree(1693,'NPzero',-,401)),
	cf(1,phi(1693,var(8))),
	cf(1,subtree(1581,'S[fin]',1933,1514)),
	cf(1,phi(1581,var(0))),
	cf(1,subtree(1539,'ROOT',-,1522)),
	cf(1,phi(1539,var(0))),
	cf(1,subtree(1522,'Sadj[fin]',-,1581)),
	cf(1,phi(1522,var(0))),
	cf(1,subtree(1514,'VPall[fin]',-,1382)),
	cf(1,phi(1514,var(0))),
	cf(1,subtree(1382,'VPv[fin]',1380,867)),
	cf(1,phi(1382,var(0))),
	cf(1,subtree(1380,'VPv[fin]',-,1373)),
	cf(1,phi(1380,var(0))),
	cf(1,subtree(1373,'V[fin]',1372,95)),
	cf(1,phi(1373,var(0))),
	cf(1,subtree(1372,'V[fin]',1371,99)),
	cf(1,phi(1372,var(0))),
	cf(1,subtree(1371,'V[fin]',1370,103)),
	cf(1,phi(1371,var(0))),
	cf(1,subtree(1370,'V[fin]',-,81)),
	cf(1,phi(1370,var(0))),
	cf(1,subtree(867,'NP',614,856)),
	cf(1,phi(867,var(2))),
	cf(1,subtree(856,'NPadj',-,781)),
	cf(1,phi(856,var(2))),
	cf(1,subtree(781,'NPzero',-,681)),
	cf(1,phi(781,var(2))),
	cf(1,subtree(681,'N',680,148)),
	cf(1,phi(681,var(2))),
	cf(1,subtree(680,'N',664,152)),
	cf(1,phi(680,var(2))),
	cf(1,subtree(664,'N',-,153)),
	cf(1,phi(664,var(2))),
	cf(1,subtree(614,'NP',-,609)),
	cf(1,phi(614,var(2))),
	cf(1,subtree(609,'D',608,112)),
	cf(1,phi(609,var(2))),
	cf(1,subtree(608,'D',607,113)),
	cf(1,phi(608,var(2))),
	cf(1,subtree(607,'D',606,115)),
	cf(1,phi(607,var(2))),
	cf(1,subtree(606,'D',605,117)),
	cf(1,phi(606,var(2))),
	cf(1,subtree(605,'D',-,122)),
	cf(1,phi(605,var(2))),
	cf(1,subtree(401,'N',400,40)),
	cf(1,phi(401,var(8))),
	cf(1,subtree(400,'N',383,44)),
	cf(1,phi(400,var(8))),
	cf(1,subtree(383,'N',-,45)),
	cf(1,phi(383,var(8))),
	cf(1,subtree(371,'NP',-,35)),
	cf(1,phi(371,var(8))),
	cf(1,subtree(199,'PERIOD',-,167)),
	cf(1,phi(199,var(0))),
	cf(1,terminal(167,'.',[167])),
	cf(1,phi(167,var(0))),
	cf(1,terminal(154,'woman',[144])),
	cf(1,phi(154,var(2))),
	cf(1,subtree(153,'N_BASE',-,154)),
	cf(1,phi(153,var(2))),
	cf(1,subtree(152,'N_SFX_BASE',-,151)),
	cf(1,phi(152,var(2))),
	cf(1,terminal(151,'+Noun',[144])),
	cf(1,phi(151,var(2))),
	cf(1,subtree(148,'NNUM_SFX_BASE',-,146)),
	cf(1,phi(148,var(2))),
	cf(1,terminal(146,'+Sg',[144])),
	cf(1,phi(146,var(2))),
	cf(1,subtree(122,'D_BASE',-,120)),
	cf(1,phi(122,var(2))),
	cf(1,terminal(120,'a',[104])),
	cf(1,phi(120,var(2))),
	cf(1,terminal(118,'+Det',[104])),
	cf(1,phi(118,var(2))),
	cf(1,subtree(117,'D_SFX_BASE',-,118)),
	cf(1,phi(117,var(2))),
	cf(1,terminal(116,'+Art',[104])),
	cf(1,phi(116,var(2))),
	cf(1,subtree(115,'D_TYPE_SFX_BASE',-,116)),
	cf(1,phi(115,var(2))),
	cf(1,terminal(114,'+Indef',[104])),
	cf(1,phi(114,var(2))),
	cf(1,subtree(113,'DEF_SFX_BASE',-,114)),
	cf(1,phi(113,var(2))),
	cf(1,subtree(112,'DNUM_SFX_BASE',-,109)),
	cf(1,phi(112,var(2))),
	cf(1,terminal(109,'+Sg',[104])),
	cf(1,phi(109,var(2))),
	cf(1,subtree(103,'V_SFX_BASE',-,101)),
	cf(1,phi(103,var(0))),
	cf(1,terminal(101,'+Verb',[71])),
	cf(1,phi(101,var(0))),
	cf(1,subtree(99,'VTNS_SFX_BASE',-,98)),
	cf(1,phi(99,var(0))),
	cf(1,terminal(98,'+Pres',[71])),
	cf(1,phi(98,var(0))),
	cf(1,terminal(96,'+3sg',[71])),
	cf(1,phi(96,var(0))),
	cf(1,subtree(95,'VPERS_SFX_BASE',-,96)),
	cf(1,phi(95,var(0))),
	cf(1,terminal(82,'love',[71])),
	cf(1,phi(82,var(0))),
	cf(1,subtree(81,'V_BASE',-,82)),
	cf(1,phi(81,var(0))),
	cf(1,terminal(46,'man',[36])),
	cf(1,phi(46,var(8))),
	cf(1,subtree(45,'N_BASE',-,46)),
	cf(1,phi(45,var(8))),
	cf(1,subtree(44,'N_SFX_BASE',-,43)),
	cf(1,phi(44,var(8))),
	cf(1,terminal(43,'+Noun',[36])),
	cf(1,phi(43,var(8))),
	cf(1,subtree(40,'NNUM_SFX_BASE',-,38)),
	cf(1,phi(40,var(8))),
	cf(1,terminal(38,'+Sg',[36])),
	cf(1,phi(38,var(8))),
	cf(1,subtree(35,'D',-,34)),
	cf(1,phi(35,var(8))),
	cf(1,terminal(34,'every',[23])),
	cf(1,phi(34,var(8))),
	cf(1,semform_data(0,35,1,6)),
	cf(1,semform_data(1,45,7,10)),
	cf(1,semform_data(2,81,11,16)),
	cf(1,semform_data(5,122,17,18)),
	cf(1,semform_data(7,153,19,24)),
	cf(1,fspan(var(0),1,25)),
	cf(1,fspan(var(8),1,10)),
	cf(1,fspan(var(2),17,24)),
	cf(1,surfaceform(23,'^ every',1,6)),
	cf(1,surfaceform(36,'man',7,10)),
	cf(1,surfaceform(71,'loves',11,16)),
	cf(1,surfaceform(104,'a_',17,18)),
	cf(1,surfaceform(144,'woman',19,24)),
	cf(1,surfaceform(167,'.',24,25))
	]).